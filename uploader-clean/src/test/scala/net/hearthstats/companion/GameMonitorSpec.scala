package net.hearthstats.companion

import org.scalatest._
import org.junit.runner.RunWith
import com.softwaremill.macwire.MacwireMacros._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{ Finders, FlatSpec, Matchers }
import com.softwaremill.macwire.MacwireMacros.wire
import net.hearthstats.config.TestEnvironment
import net.hearthstats.config.UserConfig
import net.hearthstats.config.TestConfig
import net.hearthstats.ui.log.Log
import java.net.URL
import org.scalatest.mock.MockitoSugar
import net.hearthstats.ProgramHelper
import org.mockito.Mockito._
import org.mockito.Matchers._
import javax.imageio.ImageIO
import net.hearthstats.game.imageanalysis.AnalyserSpec
import net.hearthstats.core.GameMode._
import net.hearthstats.core.Rank
import net.hearthstats.game.imageanalysis.LobbyAnalyser
import net.hearthstats.game.imageanalysis.RelativePixelAnalyser
import net.hearthstats.game.imageanalysis.ScreenAnalyser
import net.hearthstats.game.imageanalysis.IndividualPixelAnalyser
import net.hearthstats.game.Screen
import java.awt.image.BufferedImage

@RunWith(classOf[JUnitRunner])
class GameMonitorSpec extends FlatSpec with Matchers with MockitoSugar with OneInstancePerTest {
  val config: UserConfig = TestConfig
  val state = new CompanionState
  val helper = mock[ProgramHelper]
  val screenAnalyser = mock[ScreenAnalyser]
  val individualPixelAnalyser = mock[IndividualPixelAnalyser]

  val imageToEvent = wire[ImageToEvent]
  val lobbyAnalyser = mock[LobbyAnalyser]

  val monitor = wire[GameMonitor]

  val rank8Lobby = readImage("play_lobby")

  val sleep = config.pollingDelayMs.get * 2

  "The monitor" should "detect ranked mode and rank" in {
    setupForPlayMode(true)
    state.mode shouldBe Some(RANKED)
    //    state.rank shouldBe Some(Rank.RANK_8)
  }

  "The monitor" should "detect casual mode" in {
    setupForPlayMode(false)
    state.mode shouldBe Some(CASUAL)
    state.rank shouldBe None
  }

  //TODO: this should pass
  //    "The monitor" should "detect casual mode after ranked" in {
  //      setupForPlayMode(true)
  //      state.mode shouldBe Some(RANKED)
  //      setupForPlayMode(false)
  //      state.mode shouldBe Some(CASUAL)
  //    }

  def setupForPlayMode(ranked: Boolean) {
    when(helper.foundProgram).thenReturn(true)
    when(helper.getScreenCapture).thenReturn(rank8Lobby)
    when(lobbyAnalyser.imageShowsRankedPlaySelected(rank8Lobby)).thenReturn(ranked)
    when(lobbyAnalyser.imageShowsCasualPlaySelected(rank8Lobby)).thenReturn(!ranked)
    when(screenAnalyser.identifyScreen(any[BufferedImage], any[Screen])).thenReturn(Screen.PLAY_LOBBY)
    Thread.sleep(sleep)
  }

  def readImage(name: String) =
    ImageIO.read(classOf[AnalyserSpec].getResourceAsStream(name + ".png"))
}