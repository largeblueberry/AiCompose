package com.largeblueberry.data.di

import android.content.Context
import com.largeblueberry.data.repository.TranscriptionRepositoryImpl
import com.largeblueberry.ml.BasicPitchModel
import com.largeblueberry.ml.LiteRtBasicPitchModel
import com.largeblueberry.music.transcription.HummingTranscriber
import com.largeblueberry.music.transcription.ScoreTranscriber
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

/** 악보 파일(MusicXML·MIDI)을 두는 앱 내부 폴더. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ScoresDirectory

/** 채보에 쓰는 모델·후처리·저장 위치. */
@Module
@InstallIn(SingletonComponent::class)
object MlModule {

    /** 앱이 살아 있는 동안 하나만 연다(약 200KB). 쓰는 곳은 dagger.Lazy로 받아 처음 채보할 때 연다. */
    @Provides
    @Singleton
    fun provideBasicPitchModel(@ApplicationContext context: Context): BasicPitchModel =
        LiteRtBasicPitchModel.fromAssets(context)

    /** 상태가 없어 여러 스레드에서 같이 써도 된다. */
    @Provides
    @Singleton
    fun provideScoreTranscriber(): ScoreTranscriber = HummingTranscriber()

    @Provides
    @ScoresDirectory
    fun provideScoresDirectory(@ApplicationContext context: Context): File =
        File(context.filesDir, TranscriptionRepositoryImpl.SCORES_DIR)
}
