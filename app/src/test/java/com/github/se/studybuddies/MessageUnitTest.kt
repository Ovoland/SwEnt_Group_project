package com.github.se.studybuddies

import android.net.Uri
import com.github.se.studybuddies.data.Message
import com.github.se.studybuddies.data.User
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MessageUnitTest {
  private val testUser =
      User(
          "test",
          "test@email.ch",
          "test",
          Uri.parse("https://images.pexels.com/photos/6031345/pexels-photo-6031345.jpeg"),
          "offline")

  @Test
  fun testGetTime() {
    // Arrange
    val calendar =
        Calendar.getInstance().apply {
          set(2022, Calendar.JANUARY, 1, 15, 30) // 1st Jan 2022, 15:30
        }
    val timestamp = calendar.timeInMillis
    val message = Message.TextMessage("test-uid", "Hello, World!", testUser, timestamp)

    // Act
    val time = message.getTime()

    // Assert
    assertEquals("15:30", time)
  }

  @Test
  fun testGetDate() {
    // Arrange
    val calendar =
        Calendar.getInstance().apply {
          set(2022, Calendar.JANUARY, 1) // 1st Jan 2022
        }
    val timestamp = calendar.timeInMillis

    val message = Message.TextMessage("test-uid", "Hello, World!", testUser, timestamp)

    // Act
    val date = message.getDate()

    // Assert
    assertEquals("01 January", date)
  }

  @Test
  fun testEmptyMessage() {
    // Act
    val emptyMessage = Message.emptyText()
    // Assert
    assertEquals("", emptyMessage.text)
    assertEquals(0, emptyMessage.timestamp)
    assertEquals(User.empty(), emptyMessage.sender)
  }

  @Test
  fun emptyPhotoMessage() {
    // Act
    val emptyPhoto = Message.emptyPhoto()
    // Assert
    assertEquals(Uri.EMPTY, emptyPhoto.photoUri)
    assertEquals(0, emptyPhoto.timestamp)
    assertEquals(User.empty(), emptyPhoto.sender)
  }

  @Test
  fun testEmptyLinkMessage() {
    // Act
    val emptyLink = Message.emptyLink()
    // Assert
    assertEquals("", emptyLink.linkName)
    assertEquals(Uri.EMPTY, emptyLink.linkUri)
    assertEquals(0, emptyLink.timestamp)
    assertEquals(User.empty(), emptyLink.sender)
  }

  @Test
  fun testEmptyFileMessage() {
    // Act
    val emptyFile = Message.emptyFile()
    // Assert
    assertEquals("", emptyFile.fileName)
    assertEquals(Uri.EMPTY, emptyFile.fileUri)
    assertEquals(0, emptyFile.timestamp)
    assertEquals(User.empty(), emptyFile.sender)
  }

  @Test
  fun testTextMessage() {
    // Arrange
    val textMessage = Message.TextMessage("test-uid", "Hello, World!", testUser, 0)
    // Act
    val text = textMessage.text
    // Assert
    assertEquals("Hello, World!", text)
  }

  @Test
  fun testPhotoMessage() {
    // Arrange
    val photoMessage = Message.PhotoMessage("test-uid", Uri.EMPTY, testUser, 0)
    // Act
    val photoUri = photoMessage.photoUri
    // Assert
    assertEquals(Uri.EMPTY, photoUri)
  }

  @Test
  fun testLinkMessage() {
    // Arrange
    val linkMessage = Message.LinkMessage("test-uid", "", Uri.EMPTY, testUser, 0)
    // Act
    val linkUri = linkMessage.linkUri
    // Assert
    assertEquals(Uri.EMPTY, linkUri)
  }

  @Test
  fun testFileMessage() {
    // Arrange
    val fileMessage = Message.FileMessage("test-uid", "", Uri.EMPTY, testUser, 0)
    // Act
    val fileUri = fileMessage.fileUri
    // Assert
    assertEquals(Uri.EMPTY, fileUri)
  }
}
