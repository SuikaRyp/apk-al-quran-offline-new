package com.alquran.offline.ui.screens.reader

/** Recitations are original recordings; audio files are stored separately for each reciter. */
data class QuranReciter(
    val id: String,
    val name: String,
    val style: String,
    val folder: String,
    val portraitUrl: String,
    val portraitCredit: String
)

object QuranReciters {
    val all = listOf(
        QuranReciter(
            id = "husary", name = "Mahmoud Khalil Al-Husary", style = "Murattal · tenang dan jelas",
            folder = "Husary_128kbps",
            portraitUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/Hussary.jpg",
            portraitCredit = "Foto: Zubairkhan1 / Wikimedia Commons · Public Domain"
        ),
        QuranReciter(
            id = "alafasy", name = "Mishary Rashid Alafasy", style = "Murattal · lembut dan merdu",
            folder = "Alafasy_128kbps",
            portraitUrl = "https://imgproxy-images.merchbar.com/insecure/rs%3Afill%3A1400%3A1400%3A1/g%3Asm/f%3Awebp/Z3M6Ly9tZXJjaGJhci1pbWFnZXMtZ2NzL2JyYW5kL3NxdWFyZS8xNDE2MTIwLmpwZw",
            portraitCredit = "Foto dari halaman Merchbar; hak foto mengikuti pemiliknya"
        ),
        QuranReciter(
            id = "abdulbasit", name = "Abdul Basit Abdus-Samad", style = "Murattal · suara kuat dan khas",
            folder = "Abdul_Basit_Murattal_192kbps",
            portraitUrl = "https://media.way2quran.com/imgs/abdul-basit-abdul-samad-%28mujawd%29.jpg",
            portraitCredit = "Foto dari Way2Quran; hak foto mengikuti pemiliknya"
        ),
        QuranReciter(
            id = "sudais", name = "Abdurrahman As-Sudais", style = "Murattal · lantang dan khusyuk",
            folder = "Abdurrahmaan_As-Sudais_192kbps",
            portraitUrl = "https://quranicaudio.com/images/reciters/1.jpg",
            portraitCredit = "Foto profil sumber QuranicAudio"
        ),
        QuranReciter(
            id = "minshawi", name = "Mohamed Siddiq Al-Minshawi", style = "Murattal · menyentuh dan tartil",
            folder = "Minshawy_Murattal_128kbps",
            portraitUrl = "https://quranicaudio.com/images/reciters/6.jpg",
            portraitCredit = "Foto profil sumber QuranicAudio"
        )
    )
    fun byId(id: String): QuranReciter = all.firstOrNull { it.id == id } ?: all.first()
}
