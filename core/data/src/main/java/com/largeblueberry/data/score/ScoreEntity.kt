package com.largeblueberry.data.score

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.largeblueberry.data.audio.AudioRecordEntity

/**
 * 기기 안에서 만든 악보. 파일(MusicXML·MIDI)은 filesDir/scores에 있고 여기에는 경로만 둔다.
 * 녹음을 지우면 악보는 남기고 연결만 끊는다(SET_NULL). 악보는 녹음과 별개의 결과물이다.
 */
@Entity(
    tableName = "sheet_music",
    foreignKeys = [
        ForeignKey(
            entity = AudioRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["record_id"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("record_id")],
)
data class ScoreEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    @ColumnInfo(name = "record_id")
    val recordId: Int?,
    /** 생성 시각(epoch ms). 표시 형식은 화면이 정한다. */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    val bpm: Int,
    @ColumnInfo(name = "beats_per_bar")
    val beatsPerBar: Int,
    @ColumnInfo(name = "key_name")
    val keyName: String,
    @ColumnInfo(name = "note_count")
    val noteCount: Int,
    @ColumnInfo(name = "music_xml_path")
    val musicXmlPath: String,
    @ColumnInfo(name = "midi_path")
    val midiPath: String,
)
