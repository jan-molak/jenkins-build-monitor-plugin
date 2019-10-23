angular.
    module('buildMonitor.settings', [ 'buildMonitor.services', 'rzModule']).

    controller('controlPanel', ['$scope', 'cookieJar', 'townCrier',
        function ($scope, cookieJar, townCrier) {
            'use strict';

            $scope.updateCookie = function() {
                angular.forEach($scope.settings, function(value, name) {
                    $scope.$watch('settings.' + name, function(currentValue) {
                        cookieJar.put(name, currentValue);
                    });
                });
            }

            $scope.addCookie = function() {
                $scope.defaults.colourBlind     = $scope.defaults.colourBlind ? "1" : "0"
                $scope.defaults.reduceMotion    = $scope.defaults.reduceMotion ? "1" : "0"
                $scope.defaults.showBadges      = $scope.defaults.showBadges ? "1" : "0"

                $scope.settings.fontSize        = cookieJar.get('fontSize',        $scope.defaults.fontSize);
                $scope.settings.numberOfColumns = cookieJar.get('numberOfColumns', $scope.defaults.numberOfColumns);
                $scope.settings.colourBlind     = cookieJar.get('colourBlind',     $scope.defaults.colourBlind);
                $scope.settings.reduceMotion    = cookieJar.get('reduceMotion',    $scope.defaults.reduceMotion);
                $scope.settings.showBadges      = cookieJar.get('showBadges',      $scope.defaults.showBadges);

                $scope.updateCookie();

                // that's the minimum viable product .. at its tiniest
                townCrier.uponNewVersion(function() {
                    $scope.newVersionAvailable = true;
                });
            }

            $scope.resetSettings = function() {
                angular.forEach($scope.defaults, function(value, name) {
                    $scope.settings[name] = value;
                });

                $scope.updateCookie();
            }
        }]);
