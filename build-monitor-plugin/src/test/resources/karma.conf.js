// Karma configuration
// Generated on Sat Sep 28 2013 15:27:36 GMT+0100 (BST)

module.exports = function(config) {
    config.set({

        // base path, that will be used to resolve files and exclude
        basePath: '../../../',


        // frameworks to use
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            'src/test/resources/custom_matchers.js',
            'src/main/webapp/vendor/modernizr.custom.27682.js', // needed?
            'src/main/webapp/vendor/angular-1.5.8/angular.js',
            'src/main/webapp/vendor/angular-1.5.8/angular-animate.js',
            'src/main/webapp/vendor/angular-1.5.8/angular-sanitize.js',
            'src/main/webapp/vendor/angular-slider-5.4.0/rzslider.js',
            'src/main/webapp/vendor/ng-lodash-3.10.1.min.js',
            'src/main/webapp/vendor/ui-*.js',
            'src/test/resources/vendor/angular-mocks-1.5.8.js',
            'src/test/resources/vendor/sinon-1.7.3.js',
            'src/test/resources/vendor/jasmine-sinon-0.4.0.js',
            'src/test/resources/vendor/yahoo-2.9.0.min.js',
            'src/test/resources/vendor/yahoo-cookie-2.9.0.min.js',
            'src/main/webapp/scripts/**/*.js',
            'src/test/javascript/**/*Spec.js',  // todo: deprecate the "Spec" suffix in favour of ".spec.js"
            'src/test/javascript/**/*.spec.js'
        ],


        // list of files to exclude
        exclude: [

        ],

        preprocessors: {
            'src/main/webapp/scripts/**/*.js': 'coverage'
        },


        // test results reporter to use
        // possible values: 'dots', 'progress', 'junit', 'growl', 'coverage'
        reporters: ['progress', 'junit', 'coverage'],

        junitReporter: {
            outputFile: 'target/javascript/test-results.xml'
        },

        coverageReporter: {
            type: 'cobertura',
            dir: 'target/javascript/coverage/',
            file: 'coverage.xml'
        },

        // web server port
        port: 9876,

        // cli runner port
        runnerPort: 9100,


    // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS'],


        // If browser does not capture in given timeout [ms], kill it
        captureTimeout: 60000,


        // Continuous Integration mode
        // if true, it capture browsers, run tests and exit
        singleRun: true
    });
};
