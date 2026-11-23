package com.largeblueberry.ml

import com.largeblueberry.music.basicpitch.BasicPitchOutput

/**
 * Basic Pitch 모델 실행. 다른 계층(:core:data)이 쓰는 진입점이라 인터페이스로 둔다.
 * 네이티브·느림·생명주기가 있는 자원이라, 쓰는 쪽은 구현([LiteRtBasicPitchModel])을 모르고
 * 주입받기만 한다. 테스트에서는 가짜 출력을 내는 구현으로 바꿔 끼운다.
 */
interface BasicPitchModel {
    /**
     * 22,050Hz mono 오디오 전체 → unwrap된 note/onset.
     * @param onProgress 처리한 창 수 / 전체 창 수
     */
    fun predict(audio: FloatArray, onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }): BasicPitchOutput
}
