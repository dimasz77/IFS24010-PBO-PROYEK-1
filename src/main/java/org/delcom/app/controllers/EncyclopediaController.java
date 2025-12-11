package org.delcom.app.controllers;

import org.delcom.app.entities.Encyclopedia;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class EncyclopediaController {

    @GetMapping("/encyclopedia")
    public String listEntries(Model model) {
        // Kita buat data manual (Hardcoded) agar isinya langsung Tips & Trik berkualitas
        List<Encyclopedia> tips = new ArrayList<>();

        tips.add(createTip("Teknik Penyiraman", 
            "Waktu terbaik menyiram tanaman adalah pagi hari (sebelum jam 9) atau sore hari. Hindari menyiram saat matahari terik agar daun tidak terbakar.", 
            "Gunakan air suhu ruang. Siram tanahnya, bukan daunnya.", 
            "https://images.unsplash.com/photo-1599687267812-35905d212aa7?q=80&w=600&auto=format&fit=crop"));

        tips.add(createTip("Pencahayaan Tepat", 
            "Kenali kebutuhan cahaya tanamanmu. Tanaman 'Low Light' seperti Lidah Mertua bisa di dalam ruangan, sedangkan Kaktus butuh sinar matahari langsung.", 
            "Putar pot seminggu sekali agar pertumbuhan tanaman merata.", 
            "https://images.unsplash.com/photo-1598887142487-3c854d53d274?q=80&w=600&auto=format&fit=crop"));

        tips.add(createTip("Pemupukan Dasar", 
            "Berikan pupuk NPK seimbang (10-10-10) setiap bulan selama masa pertumbuhan. Kurangi pemupukan saat musim hujan/dingin.", 
            "Jangan memupuk tanaman yang sedang stress atau kering.", 
            "https://images.unsplash.com/photo-1628126235206-526053784c77?q=80&w=600&auto=format&fit=crop"));

        tips.add(createTip("Mengganti Pot (Repotting)", 
            "Ganti pot jika akar sudah keluar dari lubang drainase bawah. Pilih pot yang hanya 2-3 cm lebih besar dari pot sebelumnya.", 
            "Lakukan repotting di pagi/sore hari agar tanaman tidak kaget.", 
            "https://images.unsplash.com/photo-1463320726281-696a413703b6?q=80&w=600&auto=format&fit=crop"));

        tips.add(createTip("Mengatasi Hama", 
            "Jika melihat kutu putih, semprotkan campuran air sabun cuci piring tipis-tipis atau gunakan minyak neem.", 
            "Isolasi tanaman yang sakit agar tidak menular ke yang lain.", 
            "https://images.unsplash.com/photo-1581579439746-953a39e72355?q=80&w=600&auto=format&fit=crop"));

        tips.add(createTip("Membersihkan Daun", 
            "Debu pada daun menghambat fotosintesis. Lap daun dengan kain basah lembut atau kulit pisang bagian dalam agar mengkilap.", 
            "Lakukan ini sebulan sekali untuk tanaman indoor.", 
            "https://images.unsplash.com/photo-1596720426673-e4e14290f0cc?q=80&w=600&auto=format&fit=crop"));

        model.addAttribute("entries", tips);
        return "pages/encyclopedia"; 
    }

    // Helper untuk membuat objek Encyclopedia dengan cepat
    private Encyclopedia createTip(String title, String desc, String tips, String imageUrl) {
        Encyclopedia e = new Encyclopedia();
        e.setSpecies(title);      // Kita pakai field species sebagai JUDUL
        e.setDescription(desc);   // Penjelasan
        e.setCareTips(tips);      // Tips Singkat
        e.setImagePath(imageUrl); // URL Gambar Online
        return e;
    }
}