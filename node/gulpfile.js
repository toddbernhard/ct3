var gulp = require('gulp'),
  babel = require('gulp-babel'),
  exit = require('gulp-exit'),
  nodemon = require('gulp-nodemon'),
  jasmine = require('gulp-jasmine');

gulp.task('build', function () {
    return gulp.src(['src/*.js', 'lib/*.js'], {base: "."})
        .pipe(babel())
        .pipe(gulp.dest('dist'));
});

gulp.task('start', ['build'], function () {
  nodemon({
    script: 'dist/src/server.js',
    ignore: ['dist/**/*.*'],
    ext: 'js yml',
    env: { 'NODE_ENV': 'development' },
    tasks: ['build']
  })
});

gulp.task('test', ['build'], function () {
    return gulp.src('dist/**/*.test.js')
        .pipe(jasmine())
        .pipe(exit());
});

gulp.task('default', ['build', 'start']);
