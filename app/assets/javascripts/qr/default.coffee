class QrCode
  constructor: (@url = $("#qrCodeUrl"), @width = $("#qrCodeWidth"), @height = $("#qrCodeHeight")) ->
    widthAndHeight = Util.calculateDefaultSizes()
    @widthToBeUsed = widthAndHeight[0]
    @heightToBeUsed = widthAndHeight[1]

    @defaultSize()

  defaultSize: ->
    sizes = Util.calculateDefaultSizes()
    @width.val "#{sizes[0]}"
    @height.val "#{sizes[1]}"

  whichSizeToUse: (number) ->
    sizes = Util.calculateDefaultSizes()
    @widthToBeUsed = sizes[0]
    @heightToBeUsed = sizes[1]
    Math.min (Math.min number, @widthToBeUsed), @heightToBeUsed

class FakeConsole
  constructor: ->
  log: (message) ->
    return

logger = if console? then console else new FakeConsole()

log = (message) ->
  logger.log message

class Util
  constructor: ->
  @mainContainer = $("#wrap")
  @urlPattern = ///
    ^(https?|ftp|file)://([a-zA-Z0-9](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?\.)+(com\b|edu\b|biz\b|gov\b|in(?:t|fo)\b|mil\b|net\b|org\b|[a-zA-Z][a-zA-Z]+\b)(:\d+)?
    (/[-a-zA-Z0-9_:\@&amp;?=+,.!/~*'%\$]*)*
    ///
  @emailPattern = ///
    ^[\w-_.]+\@([\w](?:[\w-_]*[\w-_])?\.)+([a-zA-Z][a-zA-Z]+)
    ///
  @defaultWidth = 560
  @defaultHeight = 560
  @isNumber = (value) ->
    "#{value}".match /^[\d]+$/
  @calculateDefaultSizes: ->
    w = @mainContainer.width() - 50
    h = @mainContainer.height() - 50

    number = Math.min w, h
    [
      Math.min number, @defaultWidth
      Math.min number, @defaultHeight
    ]
  @trim = (text) ->
    "#{text}".replace /^\s+|\s+$/g, ""

class MessageBox
  constructor: (@messageBox = $("#messageBox"), @messageBoxTitle = $("#messageBoxTitle"), @messageBoxMessage = $("#messageBoxMessage"), @messageBoxCloseButton = $("#messageBoxCloseButton")) ->
    @messageBoxCloseButton.on "click", (event) =>
      @clearMessageBox()

  clearMessageBox: ->
    @messageBox.addClass "hidden" unless @messageBox.hasClass "hidden"
    @messageBoxTitle.html ""
    @messageBoxMessage.html ""

  showMessageBox: (title, message)->
    @messageBox.removeClass "hidden" if @messageBox.hasClass "hidden"
    @messageBoxTitle.html title
    @messageBoxMessage.html message

class QrCodeGenerator
  constructor: (@qrCode, @qrArea = $("#qrArea"), @customizedSize = $("#customizedSize"), @processUsing1 = $("#processUsing1"), @processUsing2 = $("#processUsing2"), @generateButton = $("#generateButton"),  @clearUrlButton = $("#clearUrlButton"), @resetAllButton = $("#resetAllButton"), @resultMain = $("#resultMain"), @messageBox = new MessageBox()) ->
    @setUp()

  enableCustomizedSize: ->
    @customizedSize.prop "checked", true
    @qrCode.width.prop "disabled", false
    @qrCode.height.prop "disabled", false

  disableCustomizedSize: ->
    @customizedSize.prop "checked", false
    @qrCode.width.prop "disabled", true
    @qrCode.height.prop "disabled", true
    @defaultSize()

  defaultSize: ->
    @qrCode.defaultSize()

  defaultTech: ->
    @processUsing1.prop "checked", true
    @processUsing2.prop "checked", false

  enableOrDisableResetAllButton: ->
    if @qrCode.url.val() or @customizedSize.prop("checked") or @qrArea.html().length or not @processUsing1.prop "checked"
      @resetAllButton.prop "disabled", false
    else if not @qrCode.url.val() and not @customizedSize.prop("checked") and not @qrArea.html().length and @processUsing1.prop "checked"
      @resetAllButton.prop "disabled", true

  enableOrDisableGenerateButton: ->
    if @qrCode.url.val() and @qrCode.width.val() and @qrCode.height.val()
      @generateButton.prop "disabled", false
    else
      @generateButton.prop "disabled", true

  enableOrDisableClearUrlButton: ->
    if @qrCode.url.val()
      @clearUrlButton.prop "disabled", false
    else
      @clearUrlButton.prop "disabled", true

  customizedSizeIsChecked: ->
    @customizedSize.prop "checked"

  enableAll: ->
    @qrCode.url.prop "disabled", false
    @qrCode.width.prop "disabled", false if @customizedSizeIsChecked()
    @qrCode.height.prop "disabled", false if @customizedSizeIsChecked()
    @customizedSize.prop "disabled", false

    @enableOrDisableClearUrlButton()
    @enableOrDisableResetAllButton()
    @enableOrDisableGenerateButton()

  disableAll: ->
    @qrCode.url.prop "disabled", true
    @qrCode.width.prop "disabled", true
    @qrCode.height.prop "disabled", true
    @customizedSize.prop "disabled", true
    @clearUrlButton.prop "disabled", true
    @resetAllButton.prop "disabled", true
    @generateButton.prop "disabled", true

  clearUrl: ->
    @qrCode.url.val ""
    @clearUrlButton.prop "disabled", true

  clearResult: ->
    @qrArea.html ""
    @qrArea.attr "title", ""

  resetAll: ->
    @clearUrl()
    @disableCustomizedSize()
    @defaultSize()
    @defaultTech()
    @clearResult()
    @hideResultBox()
    @messageBox.clearMessageBox()

  hideResultBox: ->
    @resultMain.addClass "hidden" unless @resultMain.hasClass "hidden"

  showResultBox: ->
    @resultMain.removeClass "hidden" if @resultMain.hasClass "hidden"

  validate: (url, width, height) ->
    message = []

    url = "#{url}"
    if (not url.match Util.emailPattern) and (not url.match Util.urlPattern)
      @messageBox.showMessageBox "Invalid URL", "The URL you entered doesn't look like a valid one. It must be an email address or URL."
      return false

    if not width? or not width or !Util.isNumber(width)
      message.push "Please enter a positive integer for the width! "
    if not height? or not height or !Util.isNumber(height)
      message.push "Please enter a positive integer for the height! "
    
    if message.length is 0
      true
    else
      alert(message.join "")
      false

  changeResultBoxSize: (width) ->
    widthToUse = parseInt(@qrCode.whichSizeToUse(width)) + 30
    @resultMain.attr "style", "width: #{widthToUse}px; margin-left: -#{widthToUse / 2}px;"
    # width: 560px;
    # margin-left: -280px;

  prepareUrlOrEmailAddress: (url) ->
    urlToUse = "#{url}"
    if urlToUse.match(Util.emailPattern) or urlToUse.indexOf("://") > 0
      url 
    else
      "http://" + url

  generate: =>
    @messageBox.clearMessageBox()
    @clearResult()
    @hideResultBox()
    url = Util.trim(@qrCode.url.val())
    width = @qrCode.whichSizeToUse(@qrCode.width.val())
    height = @qrCode.whichSizeToUse(@qrCode.height.val())
    @changeResultBoxSize(width)

    log """before preparation: {
  url: "#{url}",
  width: #{width},
  height: #{height}
}"""

    url = @prepareUrlOrEmailAddress(url)
    log """after preparation: {
  url: "#{url}",
  width: #{width},
  height: #{height}
}"""

    if (not @validate(url, width, height))
      return

    @disableAll()
    @showResultBox()
    try
      new QRCode "qrArea", {
        text: "#{url}"
        width: width
        height: height
        colorDark : "#000000"
        colorLight : "#ffffff"
        correctLevel : QRCode.CorrectLevel.H
      }
    catch error
      @messageBox.showMessageBox "System Error", "Something went wrong! Please try with Scala instead of JavaScript then see if it works. If not, please let me know.<p>Error Details: <br>#{error}</p>"
    @enableAll()

  generateUsingServer: =>
    @messageBox.clearMessageBox()
    @clearResult()
    @hideResultBox()
    url = Util.trim(@qrCode.url.val())
    width = @qrCode.whichSizeToUse(@qrCode.width.val())
    height = @qrCode.whichSizeToUse(@qrCode.height.val())

    log """before preparation: {
  "url": "#{url}",
  "width": #{width},
  "height": #{height},
}"""

    @changeResultBoxSize(width)

    url = @prepareUrlOrEmailAddress(url)
    if (not @validate(url, width, height))
      return

    log """after preparation: {
  "url": "#{url}",
  "width": #{width},
  "height": #{height}
}"""

    @disableAll()

    $.ajaxSettings.traditional = true
    $.ajaxSetup
      "type" : "POST"
      "dataType" : "json"
      "contentType" : "application/json"

    jsRoutes.controllers.QrCodeController.generateQr().ajax({
      "data" : JSON.stringify({
        "url": "#{url}",
        "width": "#{width}"
        "height": "#{height}"
      })
    }).done((response) => 
      # some code here
      @qrArea.attr "title", "#{response.url}"
      @qrArea.html """<img src="#{response.qrUrl}" width="#{response.width}" height="#{response.height}" alt="#{response.url}" title="#{response.url}" />"""
      @showResultBox()
      @enableAll()

    ).fail((jqXHR, textStatus, errorThrown) =>
      # fail
      log "jqXHR: #{JSON.stringify(jqXHR)}, textStatus: #{textStatus}, errorThrown: #{errorThrown}"
      @messageBox.showMessageBox "System Error", "Something went wrong! Please try it later again. If it happens again later, please let me know.<p>Error Details: <br>#{errorThrown}</p>"
      @hideResultBox()
      @enableAll()
    )

  setUp: ->
    @generateButton.on "click", (event) =>
      if @processUsing1.prop "checked"
        @generate()
      else
        @generateUsingServer()

    @resetAllButton.on "click", (event) =>
      @resetAll()
      @enableOrDisableResetAllButton()
      @enableOrDisableGenerateButton()

    @clearUrlButton.on "click", (event) =>
      @clearUrl()
      @enableOrDisableResetAllButton()
      @enableOrDisableGenerateButton()

    @qrCode.url.on "keyup keydown keypress focus blur", (event) =>
      @enableOrDisableClearUrlButton()
      @enableOrDisableResetAllButton()
      @enableOrDisableGenerateButton()

    @qrCode.width.on "keyup keydown keypress", (event) =>
      @enableOrDisableResetAllButton()
      @enableOrDisableGenerateButton()
    @qrCode.height.on "keyup keydown keypress", (event) =>
      @enableOrDisableResetAllButton()
      @enableOrDisableGenerateButton()

    @customizedSize.on "click", (event) =>
      if @customizedSize.prop("checked")
        @enableCustomizedSize()
      else
        @disableCustomizedSize()
      @enableOrDisableResetAllButton()
      @enableOrDisableGenerateButton()

    @processUsing1.on "click", (event) =>
      @enableOrDisableResetAllButton()

    @processUsing2.on "click", (event) =>
      @enableOrDisableResetAllButton()

kevinInitialProcess = []
kevinInitialProcess.push ->
  qrCodeGenerator = new QrCodeGenerator new QrCode()

window.kevinInitialProcess = [] unless window.kevinInitialProcess
window.kevinInitialProcess = window.kevinInitialProcess.concat kevinInitialProcess
