/*
 Modules
 */
var gulp = require('gulp'),
  util = require('gulp-util'),
  seq = require('run-sequence'),
  gulpIf = require('gulp-if'),
  gulp_shell = require('gulp-shell'),
  http = require('http'),
  post = require('request'),
  cradle = require('cradle'),
  shell = require('shelljs'),
  jshint = require('gulp-jshint'),
  csslint = require('gulp-csslint'),
  minifyCss = require('gulp-minify-css'),
  uglify = require('gulp-uglify'),
  minifyHtml = require('gulp-minify-html'),
  watch = require('gulp-watch'),
  clean = require('gulp-clean'),
  concat = require('gulp-concat'),
  eventStream = require('event-stream'),
  runSequence = require('run-sequence'),
  html2js = require('gulp-ng-html2js'),
  filelog = require('gulp-filelog'),
  compass = require('compass'),
  filter = require('gulp-filter'),
  changed = require('gulp-changed'),
  browserSync = require('browser-sync'),
  karma = require('karma').server,
  flatten = require('gulp-flatten'),
  globs = require('./globs'),
  usemin = require('gulp-usemin'),
  del = require('del');


// sleep = require('sleep');
// currencies_seed = require('./datafiles/currencies.json'),
// cities_seed = require('./datafiles/cities.json'),
// users_seed = require('./datafiles/users.json'),
// entities_seed = require('./datafiles/entities.json'),
countries_seed = require('./datafiles/countries.json').data,
admin_seed = require('./datafiles/admins.json').data,
registered_seed = require('./datafiles/registered.json').data,
db_info = {
  "cloudant-url": "http://steveshogren:mycloudant@steveshogren.cloudant.com"
},
db = null;

// override the "default settings" with those from the config file
try {
  db_info = require('./resources/config.json');
} catch (ex) {}

if (util.env.db !== undefined) {
  db_info.name = util.env.db;
} else {
  db_info.name = "";
}

db = new(cradle.Connection)(db_info["cloudant-url"]).database(db_info.name);
util.log("Connection: " + db.connection.host + " - " + db.name);

var cf_envs = {
  internal: "https://api.stage1.ng.bluemix.net",
  external: "https://api.ng.bluemix.net"
};

// util.log("Acting on: " + db_info.name);

/*
 Helpers
 */

var cleanBowerList = function(str) {
  return str.replace(/(['"])?([a-zA-Z0-9_]+)(['"])?:/g, '"$2":').replace(/'/g, '"');
};

var getArrayOfValues = function(obj) {
  var toReturn = [];
  Object.keys(obj).forEach(function(key) {
    toReturn.push(obj[key]);
  });
  return toReturn;
};

/*
 Tasks
 */

gulp.task('deployScripts', function() {
  var bower_scripts = JSON.parse(cleanBowerList('{' + shell.exec('bower list --paths', {
    silent: true
  }).output + '}'));

  var files = getArrayOfValues(bower_scripts);

  files.map(function(item) {
    util.log(item);
  });
});

gulp.task('createDb', function() {
  db.create(function(err, res) {
    util.log(res);
    if (err == null && res.ok === true) {
      util.log("Database created");
    } else {
      util.log("Database creation failed: " + err.reason);
      throw ("Database creation error");
    }
  });
});

gulp.task('which', function() {});

gulp.task('publishCountries', function() {
  countries_seed.map(function(country) {
    db.save(country);
  });
});


/*
 Helpful Variables
 */
var dev = 'resources/dev',
  prod = 'resources/public',
  bower = dev + '/bower_components',
  isProd = false;

/*
 Tasks
 */
gulp.task('default', ['js', 'html']);

gulp.task('vendorjs', function() {

  var allDeps = globs.bowerJsFiles
    .concat(globs.nonBowerVendorScripts)
    .concat(globs.missingBowerMains)
    .concat(globs.flotFiles);

  gulp.src(allDeps)
    .pipe(gulp.dest(prod + '/scripts/vendor'));

  // gulp.src(bowerJsFiles)
  //   .pipe(flatten())
  //   .pipe(gulp.dest(prod + '/vendor'));
});

gulp.task('watch', function() {
  gulp.watch(globs.sassFiles, ['sass', browserSync.reload]);
  gulp.watch(globs.jsFiles, ['js', browserSync.reload]);
  gulp.watch(globs.htmlFiles, ['html', browserSync.reload]);
  gulp.watch(globs.templateFiles, ['templates', browserSync.reload]);
  browserSync({
    proxy: 'localhost:8080',
    files: 'resources/public/**/*.*'
  });
});

gulp.task('prod', function() {
  isProd = true;
  return runSequence('minifyjs');
});

gulp.task('dev', function() {
  runSequence('clean', ['html', 'otherResources', 'js', 'sass', 'vendorjs']);
});

gulp.task('sass', function(cb) {
  compass.compile(cb);
});

gulp.task('compass', function(cb) {
  compass.compile(cb);
});

gulp.task('js', function() {
  gulp.src(globs.jsFiles)
    .pipe(gulpIf(!isProd, jshint()))
    .pipe(gulpIf(!isProd, jshint.reporter('default')))
    .pipe(changed(prod))
    .pipe(gulp.dest(prod + '/scripts'));
});

gulp.task('concatjs', function() {
  var scriptFilter = filter('**/*.js');

  return gulp.src(prod + '/index.html')
    .pipe(usemin())
    .pipe(scriptFilter)
    .pipe(gulpIf(isProd, uglify()))
    .pipe(scriptFilter.restore())
    .pipe(gulp.dest(prod + '/usemin'));
});

gulp.task('delnonminified', function() {
  return del.sync([prod + '/index.html', prod + '/scripts/**/*.js'])
})

gulp.task('copymin', function() {
  return gulp.src(prod + '/usemin/**/*.*')
    .pipe(gulp.dest(prod));
})

gulp.task('deleteusemin', function() {
  return del.sync(prod + '/usemin')
});

gulp.task('minifyjs', function() {
  return runSequence('concatjs', 'delnonminified', 'copymin', 'deleteusemin');
});

gulp.task('html', function() {
  gulp.src(globs.htmlFiles)
  // .pipe(gulpIf(isProd, minifyHtml({
  //   empty: true,
  //   quote: true
  // })))
  .pipe(changed(prod))
    .pipe(gulp.dest(prod));
});

gulp.task('otherResources', function() {
  gulp.src(globs.imageFiles)
    .pipe(changed(prod))
    .pipe(gulp.dest(prod + '/images'));

  gulp.src(globs.favicon)
    .pipe(changed(prod))
    .pipe(gulp.dest(prod));

  gulp.src(globs.fonts)
    .pipe(changed(prod))
    .pipe(gulp.dest(prod + '/fonts'));

  gulp.src(globs.resourceFiles)
    .pipe(gulp.dest(prod + '/i18n'));

  gulp.src(dev + '/styles/font-awesome.css')
    .pipe(changed(prod))
    .pipe(gulp.dest(prod + '/styles'));
});

gulp.task('templates', function() {
  gulp.src(globs.templateFiles)
    .pipe(minifyHtml({
      empty: true,
      spare: true,
      quotes: true
    }))
    .pipe(html2js({
      moduleName: "templates"
    }))
    .pipe(concat("templates.js"))
    .pipe(uglify())
    .pipe(gulp.dest(prod));
});

gulp.task('test', function(done) {
  karma.start({
    configFile: __dirname + '/karma.conf.js',
    singleRun: false,
    browsers: ['Chrome']
  }, done);
});

gulp.task('testCI', function(done) {
  karma.start({
    configFile: __dirname + '/karma.conf.js',
    singleRun: true,
    reporters: ['teamcity']
  }, done);
});


gulp.task('clean', function() {
  return del.sync(prod);
});