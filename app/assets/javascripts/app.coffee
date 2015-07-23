utils = window.angular.module('utils' , [])

utils.controller('ATMController', ($scope, $http) ->
  init = ->
    $scope.showResultFlag = true
    $scope.msgs = []
    $scope.offset = 3
    $scope.pageSize = 3
    $scope.state = "Empty"
    $scope.cash = 0

  startWS = ->
    wsUrl = jsRoutes.controllers.AppController.indexWS().webSocketURL()
    $scope.socket = new WebSocket(wsUrl)
    $scope.socket.onmessage = (msg) ->
      $scope.$apply( ->
        console.log "received : #{msg}"
        $scope.showResultFlag = true
        $scope.cash = JSON.parse(msg.data).eventInfo.totalCash
        $scope.state = JSON.parse(msg.data).eventInfo.toState
        $scope.msgs.push JSON.parse(msg.data).eventInfo
        $scope.hasMore = $scope.msgs.length > $scope.offset
       )

  $scope.deposit = ->
    $http.get(jsRoutes.controllers.AppController.deposit().url).success( ->)

  $scope.withdraw = ->
    $http.get(jsRoutes.controllers.AppController.withdraw().url).success( ->)

  $scope.recycle = ->
    $http.get(jsRoutes.controllers.AppController.recycle().url).success( ->)

  $scope.reset = ->
      $scope.state = "Empty"
      $scope.cash = 0
      $http.get(jsRoutes.controllers.AppController.reset().url).success( ->)


  $scope.sortByDate = (msg) ->
     return new Date(msg.start);

  $scope.showMore = ->
     $scope.offset = $scope.offset  + $scope.pageSize
     $scope.hasMore = $scope.msgs.length > $scope.offset

  init()
  startWS()
)

window.angular.module('app' , ['utils'])