import argparse
import glob
import os

from keras import callbacks, Model
from keras.layers import GlobalAveragePooling2D, Dense
from keras.optimizers import SGD
from keras.preprocessing.image import ImageDataGenerator
from keras.applications.mobilenet import  MobileNet, preprocess_input
from tensorflow.python.lib.io import file_io

os.system('gsutil cp -m gs://anastasia_mushrooms/data .')
FC_SIZE = 256
NB_IV3_LAYERS_TO_FREEZE = -4
BAT_SIZE = 32
NB_EPOCHS = 3

def get_nb_files(directory):
    if not os.path.exists(directory):
        return 0
    cnt = 0
    for r, dirs, files in os.walk(directory):
        for dr in dirs:
            cnt += len(glob.glob(os.path.join(r, dr + "/*")))
    return cnt

def add_new_last_layer(base_model, nb_classes):
  x = base_model.output
  x = GlobalAveragePooling2D()(x)
  x = Dense(FC_SIZE, activation='relu')(x)
  predictions = Dense(nb_classes, activation='softmax')(x)
  model = Model(input=base_model.input, output=predictions)
  return model

def setup_to_transfer_learn(model, base_model):
  """Freeze all layers and compile the model"""
  for layer in base_model.layers:
    layer.trainable = False
  model.compile(optimizer='rmsprop',
                loss='categorical_crossentropy',
                metrics=['accuracy'])

def setup_to_finetune(model):
   """Freeze the bottom NB_IV3_LAYERS and retrain the remaining top
      layers.
   note: NB_IV3_LAYERS corresponds to the top 2 inception blocks in
         the inceptionv3 architecture
   Args:
     model: keras model
   """
   for layer in model.layers[:NB_IV3_LAYERS_TO_FREEZE]:
      layer.trainable = False
   for layer in model.layers[NB_IV3_LAYERS_TO_FREEZE:]:
      layer.trainable = True
   model.compile(optimizer=SGD(lr=0.0001, momentum=0.9),
                 loss='categorical_crossentropy', metrics=['accuracy'])

def train_model(args, job_dir='./tmp/preprocessing'):
    print('preparing dataset')
    # print(nb_val_samples)
    checkpoint = callbacks.ModelCheckpoint(args.output_model_file, monitor='val_acc', verbose=1, save_best_only=True,  mode='max')
    callbacks_list = [checkpoint]
    nb_classes = 75
    nb_train_samples = get_nb_files(args.train_dir) / BAT_SIZE
    nb_val_samples = get_nb_files(args.val_dir) / BAT_SIZE
    nb_epoch = int(args.nb_epoch)
    batch_size = int(args.batch_size)

    train_datagen = ImageDataGenerator(
        preprocessing_function=preprocess_input,
        rotation_range=42,  # +
        width_shift_range=0.2,  # +
        height_shift_range=0.2,  # +
        rescale=1. / 255,
        shear_range=0.2,
        zoom_range=0.2,
        horizontal_flip=True,  # +
        vertical_flip=True,  # +
        fill_mode='nearest')


    test_datagen = ImageDataGenerator(
        preprocessing_function=preprocess_input,
        rotation_range=42,  # +
        width_shift_range=0.2,  # +
        height_shift_range=0.2,  # +
        rescale=1. / 255,
        shear_range=0.2,
        zoom_range=0.2,
        horizontal_flip=True,  # +
        vertical_flip=True,  # +
        fill_mode='nearest')

    train_datagen = train_datagen.flow_from_directory(
            args.train_dir,
            target_size=(224, 224),
            shuffle = True,
            batch_size=batch_size,
            class_mode='categorical"')

    validation_generator = test_datagen.flow_from_directory(
            args.val_dir,
            target_size=(224, 224),
            shuffle=True,
            batch_size=batch_size,
            class_mode='categorical"')

    print('setup model')

    base_model = MobileNet(weights="imagenet",
                      include_top=False,
                      input_shape=(224, 224, 3))
    model = add_new_last_layer(base_model, nb_classes)

    # transfer learning
    setup_to_transfer_learn(model, base_model)
    print('transfer learning')

    model.fit_generator(
            train_datagen,
            steps_per_epoch=nb_train_samples,
            epochs=nb_epoch,
            validation_data=validation_generator,
            validation_steps=nb_val_samples,
            class_weight='auto',
            callbacks=callbacks_list)
    model.save(args.output_model_file)

    # fine-tuning
    setup_to_finetune(model)
    print('finetune')
    model.fit_generator(
            train_datagen,
            steps_per_epoch=nb_train_samples,
            epochs=nb_epoch,
            validation_data=validation_generator,
            validation_steps=nb_val_samples,
            class_weight='auto',
            callbacks=callbacks_list)

    model.save(args.output_model_file)

    with file_io.FileIO(args.output_model_file, mode='r') as input_f:
        with file_io.FileIO(job_dir + '/mobilenet_mushrooms1.h5', mode='w+') as output_f:
            output_f.write(input_f.read())


if __name__ == '__main__':
    # Parse the input arguments for common Cloud ML Engine options
    parser = argparse.ArgumentParser()
    parser.add_argument(
      '--train_dir',
      default="/data/validation")
    parser.add_argument(
        '--val_dir',
        default="/data/test")
    parser.add_argument(
      '--job-dir')
    parser.add_argument("--output_model_file", default="/data/mobilenet_mushrooms1.h5")
    parser.add_argument("--nb_epoch", default=NB_EPOCHS)
    parser.add_argument("--batch_size", default=BAT_SIZE)
    args = parser.parse_args()
    train_model(args)