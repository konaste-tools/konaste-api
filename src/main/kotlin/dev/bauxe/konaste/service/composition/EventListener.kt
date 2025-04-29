package dev.bauxe.konaste.service.composition

abstract class EventListener {
  /**
   * [onGameBoot] is fired when a new instance of the sv6c.exe process has been found. Note that
   * this will typically fire prior to any memory availability. If you would like to utilise the
   * version functions, you should hook [onGameStart] instead
   */
  open fun onGameBoot() {}

  /**
   * [onGameStart] is fired when a game version is successfully resolved. This event will never be
   * fired on an unsupported version. Please use
   * [Konaste Decrypt API](https://decrypt.konaste.bauxe.dev/versions) to identify if your version
   * is supported.
   */
  open fun onGameStart() {}

  /**
   * [onGameClose] is fired when a game is identified as closed. That is, the game version can no
   * longer be resolved successfully.
   */
  open fun onGameClose() {}

  /**
   * [onLogin] is fired after a player logs in and their profile has been fully loaded. You will be
   * unable to retrieve any user or score data prior to this event being fired.
   */
  open fun onLogin() {}

  /**
   * [onLogout] is fired after a player logs out. You will be unable to retrieve any user or score
   * data after this event is fired. Use this to clean up any user-specific data.
   */
  open fun onLogout() {}
}
