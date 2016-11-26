var gulp = require('gulp');
var react = require('gulp-react');
var concat = require('gulp-concat');

gulp.task('default', function() {
  return gulp.src('src/**') // go to src dir and load every file
    .pipe(react())
    .pipe(concat('application.js'))
    .pipe(gulp.dest('./'))
});
