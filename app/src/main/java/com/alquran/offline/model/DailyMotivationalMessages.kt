package com.alquran.offline.model

data class MotivationalMessage(
    val text: String,
    val author: String = "Mas Alyang",
    val period: String // "pagi", "siang", atau "sore"
)

object DailyMotivationalMessages {
    
    private val morningMessages = listOf(
        MotivationalMessage(
            "Setiap pagi adalah kesempatan baru untuk menjadi versi terbaik dari diri Anda.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Mulai hari dengan niat baik dan hati yang bersih. Semoga Allah memberkahi setiap langkahmu.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Bangun dengan syukur atas nikmat hidup yang telah diberikan oleh Allah.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Pagi yang cerah membawa harapan baru. Manfaatkan waktu dengan produktif.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Sebelum memulai aktivitas, siapkan diri dengan doa dan ibadah kepada Allah.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Kesuksesan dimulai dari pagi yang penuh semangat dan komitmen.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Bersiaplah menghadapi hari dengan ketenangan hati dan kepercayaan kepada Allah.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Ingatlah bahwa setiap tantangan adalah kesempatan untuk tumbuh lebih kuat.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Pagi ini, pilih untuk bersyukur meskipun masih ada yang kurang.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Dengan niat dan usaha yang sungguh-sungguh, tidak ada yang mustahil.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Jangan biarkan kegagalan kemarin mengganggu semangat hari ini.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Mulailah dengan langkah kecil, konsistensi adalah kunci kesuksesan.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Pagi adalah waktu terbaik untuk berdoa dan mempersiapkan rencana hari ini.",
            "Mas Alyang",
            "pagi"
        ),
        MotivationalMessage(
            "Setiap orang memiliki kekuatan unik. Temukan dan kembangkan potensimu.",
            "Mas Alyang",
            "pagi"
        )
    )

    private val afternoonMessages = listOf(
        MotivationalMessage(
            "Siang ini, ambil istirahat sejenak untuk merefresh energi Anda.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Jangan lupakan waktu solat Dzuhur. Hubungan dengan Allah adalah prioritas utama.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Pertengahan hari adalah kesempatan untuk mengevaluasi pekerjaan Anda.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Motivasi terbesar datang dari kesadaran bahwa Anda berbuat untuk Allah.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Tetap fokus pada tujuan Anda meskipun tantangan bermunculan.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Istirahat adalah bagian dari produktivitas. Jangan terlalu keras pada diri sendiri.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Siang ini, beri salam dan berbagi kebaikan dengan orang-orang sekitar Anda.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Kesabaran adalah harta karun. Bersabarlah dalam menghadapi setiap cobaan.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Jangan membandingkan diri Anda dengan orang lain. Fokus pada pertumbuhan pribadi.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Setiap pencapaian kecil adalah langkah besar menuju kesuksesan.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Ambil pelajaran dari setiap pengalaman, baik itu sukses atau kegagalan.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Keikhlasan dalam setiap amal membuat hasilnya menjadi lebih bermakna.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Siang yang cerah untuk mengingatkan Anda bahwa Anda bisa melakukan apapun.",
            "Mas Alyang",
            "siang"
        ),
        MotivationalMessage(
            "Bersyukur atas kesehatan, keluarga, dan rizki yang telah diberikan.",
            "Mas Alyang",
            "siang"
        )
    )

    private val eveningMessages = listOf(
        MotivationalMessage(
            "Sore menjelang. Segera persiapkan diri untuk solat Ashar.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Evaluasi hari ini dan syukuri semua yang telah terjadi.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Sore ini adalah waktu yang tepat untuk berefleksi dan berdoa.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Jangan biarkan sore yang cerah terbuang begitu saja tanpa makna.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Kumpulkan kebersamaan dengan keluarga di sore hari ini.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Sore yang indah adalah pengingat bahwa Tuhan masih memberikan kesempatan.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Terima kasih atas semua yang telah dicapai hari ini dengan tulus hati.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Sebelum matahari terbenam, pastikan Anda sudah menunaikan solat Ashar.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Sore adalah waktu untuk istirahat mental sebelum malam tiba.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Jika hari ini ada kesalahan, maafkan diri sendiri dan mulai lagi besok.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Matahari sore mengingatkan bahwa tidak ada yang permanen, hanya perubahan.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Sore ini, berikan rasa syukur yang tulus kepada Allah atas segala nikmat.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Persiapkan malam Anda dengan hati yang tenang dan pikiran yang positif.",
            "Mas Alyang",
            "sore"
        ),
        MotivationalMessage(
            "Sore menjelang untuk mengingatkan Anda bahwa Anda telah berhasil melewati satu hari lagi.",
            "Mas Alyang",
            "sore"
        )
    )

    fun getDailyMotivation(): MotivationalMessage {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)

        return when {
            hour in 4..10 -> {
                val index = (dayOfYear % morningMessages.size)
                morningMessages[index]
            }
            hour in 11..16 -> {
                val index = (dayOfYear % afternoonMessages.size)
                afternoonMessages[index]
            }
            else -> {
                val index = (dayOfYear % eveningMessages.size)
                eveningMessages[index]
            }
        }
    }

    fun getAllMessages(): List<MotivationalMessage> {
        return morningMessages + afternoonMessages + eveningMessages
    }

    fun getMorningMessages() = morningMessages
    fun getAfternoonMessages() = afternoonMessages
    fun getEveningMessages() = eveningMessages
}
