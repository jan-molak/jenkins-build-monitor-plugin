angular.
    module('buildMonitor.settings', [ 'buildMonitor.services', 'uiSlider']).

    controller('controlPanel', ['$scope', 'cookieJar', 'townCrier', 'DEFAULT_SETTINGS_FONTSIZE', 'DEFAULT_SETTINGS_NUMBEROFCOLUMNS', 'DEFAULT_SETTINGS_COLOURBLINDMODE',
        function ($scope, cookieJar, townCrier, DEFAULT_SETTINGS_FONTSIZE, DEFAULT_SETTINGS_NUMBEROFCOLUMNS, DEFAULT_SETTINGS_COLOURBLINDMODE) {
            'use strict';

            $scope.settings.fontSize        = cookieJar.get('fontSize',        DEFAULT_SETTINGS_FONTSIZE);
            $scope.settings.numberOfColumns = cookieJar.get('numberOfColumns', DEFAULT_SETTINGS_NUMBEROFCOLUMNS);
            $scope.settings.colourBlind     = cookieJar.get('colourBlind',     DEFAULT_SETTINGS_COLOURBLINDMODE);

            angular.forEach($scope.settings, function(value, name) {
                $scope.$watch('settings.' + name, function(currentValue) {
                    cookieJar.put(name, currentValue);
                });
            });

            // that's the minimum viable product .. at its tiniest
            townCrier.uponNewVersion(function() {
                $scope.newVersionAvailable = true;
            });
        }]);