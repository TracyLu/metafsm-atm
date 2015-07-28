utils = window.angular.module('utils' , [])

utils.controller('ATMController', ($scope, $http) ->
  init = ->
    $scope.showResultFlag = true
    $scope.msgs = []
    $scope.offset = 3
    $scope.pageSize = 3
    $scope.state = "Empty"
    $scope.cash = 0
    $scope.canDeposit = true
    $scope.canWithDraw = false
    $scope.canRecycle = true
    $scope.canReset = false
    $scope.hasMore = false

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
        $scope.canDeposit = $scope.state is "Empty" or $scope.state is "NonEmpty"
        $scope.canWithDraw = $scope.state is "NonEmpty" or $scope.state is "Full"
        $scope.canRecycle = $scope.state is "Empty"
        $scope.canReset = $scope.state is "Recycled"
       )

  $scope.deposit = ->
    $http.get(jsRoutes.controllers.AppController.deposit().url).success( ->)

  $scope.withdraw = ->
    $http.get(jsRoutes.controllers.AppController.withdraw().url).success( ->)

  $scope.recycle = ->
    $http.get(jsRoutes.controllers.AppController.recycle().url).success( ->)

  $scope.reset = ->
      init()
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