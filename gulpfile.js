var gulp = require('gulp');

var changed = require('gulp-changed'),
        concat = require('gulp-concat'),
        uglify = require('gulp-uglify'),
        rename = require('gulp-rename'),
        imagemin = require('gulp-imagemin'),
        clean = require('gulp-clean'),
        htmlmin = require('gulp-htmlmin'),
        autoprefixer = require('gulp-autoprefixer'),
        minifyCSS = require('gulp-minify-css'),
        babel = require('gulp-babel'),
        ngAnnotate = require('gulp-ng-annotate'),
        sourcemaps = require('gulp-sourcemaps'),
        del = require('del'),
        runSequence = require('run-sequence');

var srcPath = './src/main/resources/static_src';
var destPath = './src/main/resources/static';

gulp.task('images', function () {
    return gulp.src(srcPath + '/images/**/*')
            .pipe(changed(destPath + '/images'))
            .pipe(imagemin())
            .pipe(gulp.dest(destPath + '/images'));
});

gulp.task('audio', function () {
    return gulp.src('./audio/**/*')
            .pipe(changed(destPath + '/audio'))
            .pipe(gulp.dest(destPath + '/audio'));
});

gulp.task('html-min', function () {
    return gulp.src(srcPath + '/**/*.html')
            .pipe(changed(destPath))
            .pipe(htmlmin({collapseWhitespace: true}))
            .pipe(gulp.dest(destPath));
});

gulp.task('css-flt', function () {
    return gulp.src([srcPath + '/css/*.css', '!' + srcPath + '/css/*.min.css'])
            .pipe(changed(destPath + '/css/'))
            .pipe(concat('concat.css'))
            .pipe(minifyCSS())
            .pipe(rename('fpvlaptracker.min.css'))
            .pipe(gulp.dest(destPath + '/css/'));
});

gulp.task('css-libs', function () {
    return gulp.src([srcPath + '/css/*.min.css', , srcPath + '/css/ngProgress.css'])
            .pipe(changed(destPath + '/css/'))
            .pipe(concat('concat.css'))
            .pipe(rename('libs.min.css'))
            .pipe(gulp.dest(destPath + '/css/'));
});

gulp.task('js-flt', function () {
    var files = [
        'libs/amChartsDirective-1.1.0.js',
        '/home/home.js',
        '/state/state.js',
        '/toplist/toplist.js',
        '/races/races.js',
        '/participants/participants.js',
        '/navigation/navigation.js',
        '/setup/setup.js',
        '/settings/settings.js',
        '/login/login.js',
        '/devicedata/devicedata.js',
        '/wlt.js'
    ];

    var filesPath = files.map(function (file) {
        return srcPath + '/js/' + file;
    });

    return gulp.src(filesPath)
            .pipe(changed(destPath + '/js/'))
            .pipe(babel({presets: ['es2015']}))
            .pipe(sourcemaps.init())
            .pipe(concat('concat.js'))
            .pipe(ngAnnotate())
            .pipe(uglify({
                mangle: false
            }).on('error', function (e) {
                console.log(e);
            }))
            .pipe(rename('fpvlaptracker.min.js'))
            .pipe(sourcemaps.write('maps'))
            .pipe(gulp.dest(destPath + '/js/'));
});

gulp.task('js-libs', function () {
    var libs = [
        'jquery-3.3.1.min.js',
        'angular-1.7.4.min.js',
        'angular-route-1.7.4.min.js',
        'angular-animate-1.7.4.min.js',
        'angular-cookies-1.7.4.min.js',
        'angular-touch-1.7.4.min.js',
        'bootstrap.min.js',
        'moment-with-locales-2.22.2.min.js',
        'ui-bootstrap-tpls-2.3.1.min.js',
        'ngDialog-1.4.0.min.js',
        'ngprogress-1.1.3.min.js',
        'sockjs-1.3.0.min.js',
        'stomp-4.0.8.min.js',
        'howler-2.0.15.min.js',
        'NoSleep-0.7.1.min.js',
        'amcharts-3.21.13/amcharts.js',
        'amcharts-3.21.13/serial.js'
    ];

    var libsPath = libs.map(function (lib) {
        return srcPath + '/js/libs/' + lib;
    });
    return gulp.src(libsPath)
            .pipe(concat('libs.min.js'))
            .pipe(gulp.dest(destPath + '/js/'));
});

gulp.task('clean-all', function () {
    return del([
        destPath + '/**/*'
    ]);
});


gulp.task('default', ['build'], function () {

});

gulp.task('build', ['clean-all'], function (cb) {
    runSequence(['images', 'audio', 'html-min', 'css-libs', 'css-flt', 'js-libs', 'js-flt'], cb);
});
