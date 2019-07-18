var gulp = require('gulp');

var changed = require('gulp-changed'),
        concat = require('gulp-concat'),
        uglify = require('gulp-uglify'),
        rename = require('gulp-rename'),
        imagemin = require('gulp-imagemin'),
        clean = require('gulp-clean'),
        htmlmin = require('gulp-htmlmin'),
        autoprefixer = require('gulp-autoprefixer'),
        cleanCSS = require('gulp-clean-css'),
        babel = require('gulp-babel'),
        ngAnnotate = require('gulp-ng-annotate'),
        sourcemaps = require('gulp-sourcemaps'),
        del = require('del'),
        injectVersion = require('gulp-inject-version');

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
    return gulp.src([
        srcPath + '/css/main.css'
    ])
            .pipe(changed(destPath + '/css/'))
            .pipe(concat('concat.css'))
            .pipe(cleanCSS())
            .pipe(rename('fpvlaptracker.min.css'))
            .pipe(gulp.dest(destPath + '/css/'));
});

gulp.task('css-libs', function () {
    var files = [
        './node_modules/bootswatch/dist/cosmo/bootstrap.min.css',
        './node_modules/ngprogress/ngProgress.css',
        srcPath + '/css/ngDialog.min.css',
        srcPath + '/css/ngDialog-theme-default.min.css',
        srcPath + '/css/ngDialog-theme-plain.min.css'
    ];

    return gulp.src(files)
            .pipe(changed(destPath + '/css/'))
            .pipe(concat('concat.css'))
            .pipe(rename('libs.min.css'))
            .pipe(gulp.dest(destPath + '/css/'));
});

gulp.task('js-flt', function () {
    var files = [
        '/home/home.js',
        '/state/state.js',
        '/participants/participants.js',
        '/navigation/navigation.js',
        '/setup/setup.js',
        '/scan/scan.js',
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
            .pipe(babel({presets: ['@babel/preset-env']}))
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
        'ui-bootstrap-tpls-2.3.1.min.js',
        'ngDialog-1.4.0.min.js',
        'stomp-4.0.8.min.js',
        'amChartsDirective-1.1.0.js'
    ];
    var nodelibs = [
        './node_modules/jquery/dist/jquery.min.js',
        './node_modules/angular/angular.min.js',
        './node_modules/angular-route/angular-route.min.js',
        './node_modules/angular-animate/angular-animate.min.js',
        './node_modules/angular-cookies/angular-cookies.min.js',
        './node_modules/angular-touch/angular-touch.min.js',
        './node_modules/bootstrap/dist/js/bootstrap.min.js',
        './node_modules/moment/min/moment-with-locales.min.js',
        './node_modules/ngprogress/build/ngprogress.min.js',
        './node_modules/howler/dist/howler.min.js',
        './node_modules/nosleep.js/dist/NoSleep.min.js',
        './node_modules/amcharts3/amcharts/amcharts.js',
        './node_modules/amcharts3/amcharts/serial.js',
        './node_modules/sockjs-client/dist/sockjs.min.js'
    ];

    var libsPath = libs.map(function (lib) {
        return srcPath + '/js/libs/' + lib;
    });
    libsPath = nodelibs.concat(libsPath);
    return gulp.src(libsPath)
            .pipe(concat('libs.min.js'))
            .pipe(gulp.dest(destPath + '/js/'));
});

gulp.task('clean-all', function () {
    return del([
        destPath + '/**/*'
    ]);
});


gulp.task('build', gulp.series('clean-all', gulp.parallel('images', 'audio', 'html-min', 'css-libs', 'js-libs'), gulp.parallel('css-flt', 'js-flt'), function (cb) {
    return gulp.src(srcPath + '/index.html')
            .pipe(injectVersion({
                package_file: 'build/package_version.json'
            }))
            .pipe(gulp.dest(destPath));
    cb();
}));

gulp.task('default', gulp.series('build', function (cb) {
    cb();
}));


