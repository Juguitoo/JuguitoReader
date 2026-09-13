package com.juguito.juguitoreader.data.backup

import com.google.common.truth.Truth.assertThat
import org.json.JSONException
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupManifestTest {

    private val currentDbVersion = 12

    private fun manifest(
        formatVersion: Int = 1,
        appVersionName: String = "1.2.0",
        dbVersion: Int = 12,
        createdAtEpochMs: Long = 1_725_000_000_000L,
        bookFileCount: Int = 3,
        coverFileCount: Int = 2,
    ) = BackupManifest(
        formatVersion = formatVersion,
        appVersionName = appVersionName,
        dbVersion = dbVersion,
        createdAtEpochMs = createdAtEpochMs,
        bookFileCount = bookFileCount,
        coverFileCount = coverFileCount,
    )

    @Test
    fun `validate same db version returns OK`() {
        assertThat(manifest(dbVersion = 12).validate(currentDbVersion))
            .isEqualTo(BackupValidation.OK)
    }

    @Test
    fun `validate older migratable db version returns OK`() {
        assertThat(manifest(dbVersion = 11).validate(currentDbVersion))
            .isEqualTo(BackupValidation.OK)
    }

    @Test
    fun `validate minimum migratable db version returns OK`() {
        assertThat(manifest(dbVersion = 6).validate(currentDbVersion))
            .isEqualTo(BackupValidation.OK)
    }

    @Test
    fun `validate unknown format returns UnsupportedFormat`() {
        assertThat(manifest(formatVersion = 2).validate(currentDbVersion))
            .isEqualTo(BackupValidation.UnsupportedFormat)
    }

    @Test
    fun `validate unknown format wins over too old db`() {
        assertThat(manifest(formatVersion = 2, dbVersion = 5).validate(currentDbVersion))
            .isEqualTo(BackupValidation.UnsupportedFormat)
    }

    @Test
    fun `validate db below minimum returns DbTooOld`() {
        assertThat(manifest(dbVersion = 5).validate(currentDbVersion))
            .isEqualTo(BackupValidation.DbTooOld)
    }

    @Test
    fun `validate db newer than app returns DbTooNew`() {
        assertThat(manifest(dbVersion = 13).validate(currentDbVersion))
            .isEqualTo(BackupValidation.DbTooNew)
    }

    @Test
    fun `toJson roundtrip preserves fields`() {
        val original = manifest()

        val parsed = BackupManifest.fromJson(original.toJson())

        assertThat(parsed).isEqualTo(original)
    }

    @Test
    fun `fromJson with missing keys throws JSONException`() {
        assertThrows(JSONException::class.java) {
            BackupManifest.fromJson("{}")
        }
    }

    @Test
    fun `fromJson with invalid json throws JSONException`() {
        assertThrows(JSONException::class.java) {
            BackupManifest.fromJson("not-json")
        }
    }
}
