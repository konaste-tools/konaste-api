package dev.bauxe.konaste.service.memory.polling

import dev.bauxe.konaste.models.info.GameWindow
import dev.bauxe.konaste.repository.state.GameStateRepository
import dev.bauxe.konaste.service.composition.EventManager
import dev.bauxe.konaste.service.polling.GameWindowPoller
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

class GameWindowPollerTest :
    FunSpec({
      coroutineTestScope = true
      val gameStateRepository = mockk<GameStateRepository>()
      val eventManager = mockk<EventManager>()

      beforeAny { clearMocks(gameStateRepository) }

      test("Poll will onStart once when window changes") {
        val scheduler = TestCoroutineScheduler()
        val gameWindowPoller =
            GameWindowPoller(
                StandardTestDispatcher(scheduler),
                eventManager,
                100.milliseconds,
                gameStateRepository)

        val mockFn = mockk<suspend () -> Unit>()
        coEvery { mockFn.invoke() } returns Unit
        gameWindowPoller.addOnStart(GameWindow.UI_BOOT, mockFn)

        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(GameWindow.UI_UNKNOWN, GameWindow.UI_BOOT)

        scheduler.advanceTimeBy(500.milliseconds)
        gameWindowPoller.cancel()

        coVerify(exactly = 1) { mockFn.invoke() }
        coVerify(exactly = 5) { gameStateRepository.getCurrentUi() }
      }

      test("Poll will onEnd once when window changes") {
        val gameStateRepository = mockk<GameStateRepository>()
        val scheduler = TestCoroutineScheduler()
        val gameWindowPoller =
            GameWindowPoller(
                StandardTestDispatcher(scheduler),
                eventManager,
                100.milliseconds,
                gameStateRepository)

        val mockFn = mockk<suspend () -> Unit>()
        coEvery { mockFn.invoke() } returns Unit
        gameWindowPoller.addOnEnd(GameWindow.UI_UNKNOWN, mockFn)

        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(GameWindow.UI_UNKNOWN, GameWindow.UI_BOOT)

        scheduler.advanceTimeBy(500.milliseconds)
        gameWindowPoller.cancel()

        coVerify(exactly = 1) { mockFn.invoke() }
        coVerify(exactly = 5) { gameStateRepository.getCurrentUi() }
      }

      test("Poll will not call anything when no match") {
        val scheduler = TestCoroutineScheduler()
        val gameWindowPoller =
            GameWindowPoller(
                StandardTestDispatcher(scheduler),
                eventManager,
                100.milliseconds,
                gameStateRepository)

        val mockFn = mockk<suspend () -> Unit>()
        coEvery { mockFn.invoke() } returns Unit
        gameWindowPoller.addOnStart(GameWindow.UI_SKILL_ANALYZER, mockFn)
        gameWindowPoller.addOnEnd(GameWindow.UI_START_CREDIT, mockFn)

        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(GameWindow.UI_UNKNOWN, GameWindow.UI_BOOT, GameWindow.UI_DEMO)

        scheduler.advanceTimeBy(500.milliseconds)
        gameWindowPoller.cancel()

        coVerify(exactly = 0) { mockFn.invoke() }
        coVerify(exactly = 5) { gameStateRepository.getCurrentUi() }
      }

      test("Function can be removed from onStart poller") {
        val scheduler = TestCoroutineScheduler()
        val gameWindowPoller =
            GameWindowPoller(
                StandardTestDispatcher(scheduler),
                eventManager,
                100.milliseconds,
                gameStateRepository)

        val mockFn = mockk<suspend () -> Unit>()
        gameWindowPoller.addOnStart(GameWindow.UI_BOOT, mockFn)
        gameWindowPoller.removeOnStart(GameWindow.UI_BOOT, mockFn)

        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(GameWindow.UI_UNKNOWN, GameWindow.UI_BOOT)

        scheduler.advanceTimeBy(500.milliseconds)
        gameWindowPoller.cancel()

        coVerify(exactly = 0) { mockFn.invoke() }
        coVerify(exactly = 5) { gameStateRepository.getCurrentUi() }
      }

      test("Function can be removed from onEnd poller") {
        val scheduler = TestCoroutineScheduler()
        val gameWindowPoller =
            GameWindowPoller(
                StandardTestDispatcher(scheduler),
                eventManager,
                100.milliseconds,
                gameStateRepository)

        val mockFn = mockk<suspend () -> Unit>()
        gameWindowPoller.addOnEnd(GameWindow.UI_BOOT, mockFn)
        gameWindowPoller.removeOnEnd(GameWindow.UI_BOOT, mockFn)

        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(GameWindow.UI_UNKNOWN, GameWindow.UI_BOOT)

        scheduler.advanceTimeBy(500.milliseconds)
        gameWindowPoller.cancel()

        coVerify(exactly = 0) { mockFn.invoke() }
        coVerify(exactly = 5) { gameStateRepository.getCurrentUi() }
      }

      test("OnChange receives all window changes") {
        val scheduler = TestCoroutineScheduler()
        val gameWindowPoller =
            GameWindowPoller(
                StandardTestDispatcher(scheduler),
                eventManager,
                100.milliseconds,
                gameStateRepository)

        val mockFn = mockk<suspend (GameWindow, GameWindow) -> Unit>()
        coEvery { mockFn.invoke(any(), any()) } returns Unit
        gameWindowPoller.addOnChange(mockFn)

        coEvery { gameStateRepository.getCurrentUi() } returnsMany
            listOf(
                GameWindow.UI_UNKNOWN,
                GameWindow.UI_BOOT,
                GameWindow.UI_START_CREDIT,
                GameWindow.UI_UNKNOWN)

        scheduler.advanceTimeBy(500.milliseconds)
        gameWindowPoller.cancel()

        coVerifyOrder {
          mockFn.invoke(GameWindow.UI_UNKNOWN, GameWindow.UI_BOOT)
          mockFn.invoke(GameWindow.UI_BOOT, GameWindow.UI_START_CREDIT)
          mockFn.invoke(GameWindow.UI_START_CREDIT, GameWindow.UI_UNKNOWN)
        }
        coVerify(exactly = 5) { gameStateRepository.getCurrentUi() }
      }
    })
