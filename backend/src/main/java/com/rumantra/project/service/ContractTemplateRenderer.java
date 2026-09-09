package com.rumantra.project.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.rumantra.project.dto.ContractTermsSnapshot;

/**
 * Renders a contract snapshot into readable Markdown, once per language. Both renderings come from
 * the same snapshot and are produced together, which is why the contract's content hash is taken
 * over the snapshot rather than over either body.
 *
 * <p>The legal copy here is placeholder text pending review. The commercial terms it wraps -- fee,
 * timeline, and the per-phase deliverables and revision rounds -- are not: they are read straight
 * from the accepted bid and are what the signatures actually attest to.
 */
@Component
public class ContractTemplateRenderer {

  /** Bumped whenever the template changes; it participates in the content hash. */
  public static final String TEMPLATE_VERSION = "v1";

  private static final Locale ID = new Locale("id", "ID");

  public String render(String lang, ContractTermsSnapshot terms) {
    return "id".equalsIgnoreCase(lang) ? renderId(terms) : renderEn(terms);
  }

  private String renderEn(ContractTermsSnapshot t) {
    StringBuilder sb = new StringBuilder();

    sb.append("# Architectural Design Services Agreement\n\n");
    sb.append("This Agreement is entered into through the Rumantra platform between:\n\n");
    sb.append("**The Client** — ").append(nz(t.getClientName())).append("  \n");
    sb.append("**The Architect** — ").append(nz(t.getArchitectName()));
    if (notBlank(t.getArchitectCompany())) {
      sb.append(" (").append(t.getArchitectCompany()).append(")");
    }
    if (notBlank(t.getArchitectCity())) {
      sb.append(", ").append(t.getArchitectCity());
    }
    sb.append("\n\n");
    sb.append(
        "collectively \"the Parties\", in respect of the project described below. Rumantra is not"
            + " a party to this Agreement; it operates the platform, holds phase payments, and"
            + " releases them according to Article 3.\n\n");

    sb.append("## 1. The Project\n\n");
    sb.append("**Title:** ").append(nz(t.getProjectTitle())).append("  \n");
    if (notBlank(t.getProjectCity())) {
      sb.append("**Location:** ").append(t.getProjectCity()).append("  \n");
    }
    if (notBlank(t.getProjectCategory())) {
      sb.append("**Category:** ").append(t.getProjectCategory()).append("  \n");
    }
    sb.append("\n### Scope of work\n\n");
    sb.append(notBlank(t.getScopeOfWork()) ? t.getScopeOfWork() : "_As described in the brief._");
    sb.append("\n\n");

    sb.append("## 2. Fee and Timeline\n\n");
    sb.append("**Total design fee:** ").append(money(t.getTotalFee(), Locale.US)).append("  \n");
    sb.append("**Agreed timeline:** ")
        .append(t.getTimelineDays() == null ? "—" : t.getTimelineDays() + " calendar days")
        .append(" from the commencement of the first phase.\n\n");
    sb.append(
        "The fee is fixed for the scope set out in Article 1. Work outside that scope is not"
            + " covered by this Agreement and requires a separate arrangement between the"
            + " Parties.\n\n");

    sb.append("## 3. Phases, Deliverables and Payment\n\n");
    sb.append(
        "The work is divided into the phases below. Each phase is invoiced separately. The Client"
            + " funds a phase before it begins; the funds are held by the platform and released to"
            + " the Architect only after the Client approves that phase's deliverables.\n\n");
    sb.append(phaseTableEn(t));
    sb.append("\n");
    sb.append(phaseDetailEn(t));

    sb.append("## 4. Revisions\n\n");
    sb.append(
        "Each phase carries the number of revision rounds stated above. A revision round covers"
            + " every deliverable the Client selects when requesting it, and is counted once."
            + " Revisions beyond the stated allowance, and any request that changes the scope"
            + " rather than refining the work, are chargeable at rates agreed in writing.\n\n");

    sb.append("## 5. Intellectual Property\n\n");
    sb.append(
        "The Architect retains authorship and moral rights in the design. Upon full payment of a"
            + " phase, the Client receives a licence to use that phase's deliverables for the"
            + " construction, financing, permitting and maintenance of the Project at the stated"
            + " location. The Client may not reuse the design for a different site or resell it"
            + " without the Architect's written consent. The Architect may show the work in a"
            + " portfolio unless the Parties agree otherwise in writing.\n\n");

    sb.append("## 6. Delay, Suspension and Termination\n\n");
    sb.append(
        "Either Party may terminate this Agreement by written notice through the platform. On"
            + " termination, phases already approved remain payable and are released to the"
            + " Architect; funds held for phases not yet delivered are returned to the Client;"
            + " a phase in progress is settled in proportion to the work actually delivered, as"
            + " agreed between the Parties or, failing agreement, under Article 7.\n\n");

    sb.append("## 7. Disputes\n\n");
    sb.append(
        "A Client who considers a phase's deliverables incomplete may raise a dispute through the"
            + " platform before approving it, which suspends release of that phase's funds while"
            + " the Parties confer, with Rumantra support available to mediate. Disputes not"
            + " resolved that way are governed by the laws of the Republic of Indonesia and"
            + " settled by the competent court in the Architect's domicile.\n\n");

    sb.append("## 8. Acceptance\n\n");
    sb.append(
        "Each Party accepts this Agreement by typing their own full name as an electronic"
            + " signature. Both signatures are recorded against this document with the time they"
            + " were given, and the Agreement takes effect once both Parties have signed and"
            + " confirmed the project.\n");

    return sb.toString();
  }

  private String renderId(ContractTermsSnapshot t) {
    StringBuilder sb = new StringBuilder();

    sb.append("# Perjanjian Jasa Perancangan Arsitektur\n\n");
    sb.append("Perjanjian ini dibuat melalui platform Rumantra antara:\n\n");
    sb.append("**Pemberi Tugas (Klien)** — ").append(nz(t.getClientName())).append("  \n");
    sb.append("**Arsitek** — ").append(nz(t.getArchitectName()));
    if (notBlank(t.getArchitectCompany())) {
      sb.append(" (").append(t.getArchitectCompany()).append(")");
    }
    if (notBlank(t.getArchitectCity())) {
      sb.append(", ").append(t.getArchitectCity());
    }
    sb.append("\n\n");
    sb.append(
        "selanjutnya disebut \"Para Pihak\", sehubungan dengan proyek yang diuraikan di bawah ini."
            + " Rumantra bukan pihak dalam Perjanjian ini; Rumantra mengoperasikan platform,"
            + " menahan pembayaran tiap tahap, dan menyalurkannya sesuai Pasal 3.\n\n");

    sb.append("## 1. Proyek\n\n");
    sb.append("**Judul:** ").append(nz(t.getProjectTitle())).append("  \n");
    if (notBlank(t.getProjectCity())) {
      sb.append("**Lokasi:** ").append(t.getProjectCity()).append("  \n");
    }
    if (notBlank(t.getProjectCategory())) {
      sb.append("**Kategori:** ").append(t.getProjectCategory()).append("  \n");
    }
    sb.append("\n### Lingkup pekerjaan\n\n");
    sb.append(
        notBlank(t.getScopeOfWork())
            ? t.getScopeOfWork()
            : "_Sebagaimana diuraikan dalam brief proyek._");
    sb.append("\n\n");

    sb.append("## 2. Biaya dan Jangka Waktu\n\n");
    sb.append("**Total biaya desain:** ").append(money(t.getTotalFee(), ID)).append("  \n");
    sb.append("**Jangka waktu:** ")
        .append(t.getTimelineDays() == null ? "—" : t.getTimelineDays() + " hari kalender")
        .append(" terhitung sejak tahap pertama dimulai.\n\n");
    sb.append(
        "Biaya bersifat tetap untuk lingkup pada Pasal 1. Pekerjaan di luar lingkup tersebut tidak"
            + " tercakup dalam Perjanjian ini dan memerlukan kesepakatan tersendiri antara Para"
            + " Pihak.\n\n");

    sb.append("## 3. Tahapan, Keluaran dan Pembayaran\n\n");
    sb.append(
        "Pekerjaan dibagi ke dalam tahapan berikut. Setiap tahap ditagihkan terpisah. Klien"
            + " mendanai suatu tahap sebelum tahap tersebut dimulai; dana ditahan oleh platform dan"
            + " baru disalurkan kepada Arsitek setelah Klien menyetujui keluaran tahap"
            + " tersebut.\n\n");
    sb.append(phaseTableId(t));
    sb.append("\n");
    sb.append(phaseDetailId(t));

    sb.append("## 4. Revisi\n\n");
    sb.append(
        "Setiap tahap memuat jumlah putaran revisi sebagaimana tercantum di atas. Satu putaran"
            + " revisi mencakup seluruh keluaran yang dipilih Klien pada saat pengajuan dan"
            + " dihitung satu kali. Revisi melebihi jatah tersebut, serta permintaan yang mengubah"
            + " lingkup dan bukan menyempurnakan pekerjaan, dikenakan biaya sesuai kesepakatan"
            + " tertulis.\n\n");

    sb.append("## 5. Hak Kekayaan Intelektual\n\n");
    sb.append(
        "Arsitek tetap memegang hak cipta dan hak moral atas rancangan. Setelah suatu tahap dibayar"
            + " lunas, Klien memperoleh lisensi untuk menggunakan keluaran tahap tersebut guna"
            + " pembangunan, pendanaan, perizinan dan pemeliharaan Proyek pada lokasi yang"
            + " disebutkan. Klien tidak boleh menggunakan kembali rancangan untuk lokasi lain atau"
            + " menjualnya tanpa persetujuan tertulis Arsitek. Arsitek berhak menampilkan karya"
            + " dalam portofolio kecuali Para Pihak menyepakati lain secara tertulis.\n\n");

    sb.append("## 6. Keterlambatan, Penghentian Sementara dan Pengakhiran\n\n");
    sb.append(
        "Masing-masing Pihak dapat mengakhiri Perjanjian ini melalui pemberitahuan tertulis di"
            + " platform. Pada saat pengakhiran, tahap yang telah disetujui tetap wajib dibayar dan"
            + " disalurkan kepada Arsitek; dana yang ditahan untuk tahap yang belum diserahkan"
            + " dikembalikan kepada Klien; tahap yang sedang berjalan diselesaikan secara"
            + " proporsional terhadap pekerjaan yang telah diserahkan, sesuai kesepakatan Para"
            + " Pihak atau, bila tidak tercapai, menurut Pasal 7.\n\n");

    sb.append("## 7. Penyelesaian Perselisihan\n\n");
    sb.append(
        "Klien yang menilai keluaran suatu tahap belum lengkap dapat mengajukan sengketa melalui"
            + " platform sebelum menyetujuinya, yang menangguhkan penyaluran dana tahap tersebut"
            + " selama Para Pihak berunding, dengan dukungan tim Rumantra sebagai mediator."
            + " Perselisihan yang tidak selesai dengan cara tersebut tunduk pada hukum Republik"
            + " Indonesia dan diselesaikan pada pengadilan yang berwenang di domisili Arsitek.\n\n");

    sb.append("## 8. Persetujuan\n\n");
    sb.append(
        "Masing-masing Pihak menyetujui Perjanjian ini dengan mengetikkan nama lengkapnya sendiri"
            + " sebagai tanda tangan elektronik. Kedua tanda tangan dicatat pada dokumen ini beserta"
            + " waktu pemberiannya, dan Perjanjian berlaku setelah kedua Pihak menandatangani serta"
            + " mengonfirmasi proyek.\n");

    return sb.toString();
  }

  private String phaseTableEn(ContractTermsSnapshot t) {
    StringBuilder sb = new StringBuilder();
    sb.append("| # | Phase | Fee | Share | Duration | Revisions |\n");
    sb.append("|---|---|---|---|---|---|\n");
    for (ContractTermsSnapshot.Phase p : phases(t)) {
      sb.append("| ")
          .append(p.getPhaseNumber())
          .append(" | ")
          .append(nz(p.getTitle(), "Phase " + p.getPhaseNumber()))
          .append(" | ")
          .append(money(p.getAmount(), Locale.US))
          .append(" | ")
          .append(share(p.getAmount(), t.getTotalFee()))
          .append(" | ")
          .append(p.getEstimatedDays() == null ? "—" : p.getEstimatedDays() + " days")
          .append(" | ")
          .append(p.getRevisionRounds() == null ? "—" : String.valueOf(p.getRevisionRounds()))
          .append(" |\n");
    }
    sb.append("| | **Total** | **")
        .append(money(t.getTotalFee(), Locale.US))
        .append("** | | | |\n");
    return sb.toString();
  }

  private String phaseTableId(ContractTermsSnapshot t) {
    StringBuilder sb = new StringBuilder();
    sb.append("| # | Tahap | Biaya | Porsi | Durasi | Revisi |\n");
    sb.append("|---|---|---|---|---|---|\n");
    for (ContractTermsSnapshot.Phase p : phases(t)) {
      sb.append("| ")
          .append(p.getPhaseNumber())
          .append(" | ")
          .append(nz(p.getTitle(), "Tahap " + p.getPhaseNumber()))
          .append(" | ")
          .append(money(p.getAmount(), ID))
          .append(" | ")
          .append(share(p.getAmount(), t.getTotalFee()))
          .append(" | ")
          .append(p.getEstimatedDays() == null ? "—" : p.getEstimatedDays() + " hari")
          .append(" | ")
          .append(p.getRevisionRounds() == null ? "—" : String.valueOf(p.getRevisionRounds()))
          .append(" |\n");
    }
    sb.append("| | **Total** | **").append(money(t.getTotalFee(), ID)).append("** | | | |\n");
    return sb.toString();
  }

  private String phaseDetailEn(ContractTermsSnapshot t) {
    StringBuilder sb = new StringBuilder();
    for (ContractTermsSnapshot.Phase p : phases(t)) {
      sb.append("### Phase ")
          .append(p.getPhaseNumber())
          .append(" — ")
          .append(nz(p.getTitle(), "Untitled"))
          .append("\n\n");
      sb.append("Fee ")
          .append(money(p.getAmount(), Locale.US))
          .append(". ")
          .append(
              p.getRevisionRounds() == null
                  ? "Revision allowance to be agreed."
                  : p.getRevisionRounds() + " revision round(s) included.")
          .append("\n\nDeliverables:\n\n");
      if (p.getDeliverables() == null || p.getDeliverables().isEmpty()) {
        sb.append("- _None listed._\n");
      } else {
        for (String d : p.getDeliverables()) {
          sb.append("- ").append(humanise(d)).append("\n");
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private String phaseDetailId(ContractTermsSnapshot t) {
    StringBuilder sb = new StringBuilder();
    for (ContractTermsSnapshot.Phase p : phases(t)) {
      sb.append("### Tahap ")
          .append(p.getPhaseNumber())
          .append(" — ")
          .append(nz(p.getTitle(), "Tanpa judul"))
          .append("\n\n");
      sb.append("Biaya ")
          .append(money(p.getAmount(), ID))
          .append(". ")
          .append(
              p.getRevisionRounds() == null
                  ? "Jatah revisi disepakati kemudian."
                  : "Termasuk " + p.getRevisionRounds() + " putaran revisi.")
          .append("\n\nKeluaran:\n\n");
      if (p.getDeliverables() == null || p.getDeliverables().isEmpty()) {
        sb.append("- _Tidak ada yang dicantumkan._\n");
      } else {
        for (String d : p.getDeliverables()) {
          sb.append("- ").append(humanise(d)).append("\n");
        }
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  private List<ContractTermsSnapshot.Phase> phases(ContractTermsSnapshot t) {
    return t.getPhases() == null ? List.of() : t.getPhases();
  }

  private String share(BigDecimal amount, BigDecimal total) {
    if (amount == null || total == null || total.signum() == 0) {
      return "—";
    }
    return amount
            .multiply(BigDecimal.valueOf(100))
            .divide(total, 1, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        + "%";
  }

  private String money(BigDecimal value, Locale locale) {
    if (value == null) {
      return "—";
    }
    NumberFormat format = NumberFormat.getNumberInstance(locale);
    format.setMaximumFractionDigits(0);
    return "IDR " + format.format(value);
  }

  /** Deliverable codes are stored as SITE_PLAN-style taxonomy keys. */
  private String humanise(String code) {
    if (code == null || code.isBlank()) {
      return "—";
    }
    String[] words = code.trim().replace('_', ' ').toLowerCase(Locale.ROOT).split("\\s+");
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return sb.toString();
  }

  private boolean notBlank(String value) {
    return value != null && !value.isBlank();
  }

  private String nz(String value) {
    return nz(value, "—");
  }

  private String nz(String value, String fallback) {
    return notBlank(value) ? value : fallback;
  }
}
