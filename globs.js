var dev = 'resources/dev',
  prod = 'resources/public',
  bower = dev + '/bower_components';

module.exports = {
  htmlFiles: [
    dev + '/**/*.html'
  ],
  templateFiles: dev + '/**/*.tpl.html',
  sassFiles: dev + '/**/*.{scss,sass}',
  jsFiles: [
    dev + '/scripts/**/*.js',
  ],
  resourceFiles: [dev + '/i18n/*.js'],
  flotFiles: [
    bower + '/flot/*.js',
    '!' + bower + '/flot/jquery.js'
  ],
  missingBowerMains: [
    bower + '/jquery-spinner/dist/*.min.js',
    bower + '/textAngular/dist/*.js',
    bower + '/jquery.easy-pie-chart/dist/*.min.js',
    bower + '/bootstrap-file-input/*.js'
  ],
  imageFiles: [
    dev + '/images/**/*.png',
    dev + '/images/**/*.jpg',
    dev + '/images/**/*.gif',
  ],
  favicon: dev + '/favicon.ico',
  fonts: dev + '/fonts/**/*.*',
  nonBowerVendorScripts: [
    dev + "/vendor/responsive-tables.js",
    dev + "/vendor/jquery.sparkline.min.js",
    dev + "/vendor/skycons.js"
  ]
};