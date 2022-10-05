
var exec = require('cordova/exec');

var PLUGIN_NAME = 'MultiCameraPleno';

var MultiCameraPleno = {
  takePicture: function (name, successCallback, errorCallback){
        exec(successCallback, errorCallback, PLUGIN_NAME, "takePicture", [name]);
  },
  getTakePhoto: function (name, successCallback, errorCallback){
        exec(successCallback, errorCallback, PLUGIN_NAME, "getTakePhoto", [name]);
  }
};

module.exports = MultiCameraPleno;
