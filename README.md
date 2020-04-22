# @react-native-hero/fs

## Getting started

Install the library using either Yarn:

```
yarn add @react-native-hero/fs
```

or npm:

```
npm install --save @react-native-hero/fs
```

## Link

- React Native v0.60+

For iOS, use `cocoapods` to link the package.

run the following command:

```
$ cd ios && pod install
```

For android, the package will be linked automatically on build.

- React Native <= 0.59

run the following command to link the package:

```
$ react-native link @react-native-hero/fs
```

## Example

```js
import {
  CODE,
  DIRECTORY,
  exists,
  unlink,
  stat,
  md5,
  preview,
} from '@react-native-hero/fs'

exists(path)
.then(data => {
  if (data.exists) {

  }
})

stat(path)
.then(data => {
  data.name
  data.size
})
.catch(err => {
  if (err.code === CODE.FILE_NOT_FOUND) {
    console.log('file is not found')
  }
})

unlink(path)
.then(data => {
  if (data.success) {

  }
})
.catch(err => {
  if (err.code === CODE.FILE_NOT_FOUND) {
    console.log('file is not found')
  }
})

md5(path)
.then(data => {
  data.md5
})
.catch(err => {
  if (err.code === CODE.FILE_NOT_FOUND) {
    console.log('file is not found')
  }
})

preview({
  path: '/xx/xx.pdf',
  mimeType: 'application/pdf',
})
.then(() => {
  // success
})
.catch(err => {
  if (err.code === CODE.FILE_NOT_FOUND) {
    console.log('file is not found')
  }
  else if (err.code === CODE.PREVIEW_APP_NOT_FOUND) {
    console.log('preview app is not found')
  }
})

// android only
scan({
  path: '/xx/xx.jpg',
  mimeType: 'image/jpeg',
})
.then(data => {
  data.path
})
.catch(err => {
  if (err.code === CODE.SCANNER_NOT_CONNECTED) {
    console.log('scanner is not connected.')
  }
})
```
