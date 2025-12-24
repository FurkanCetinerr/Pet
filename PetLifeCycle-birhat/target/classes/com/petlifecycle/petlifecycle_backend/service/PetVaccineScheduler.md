# PetVaccineScheduler Servisi

`src/main/java/com/petlifecycle/petlifecycle_backend/service/PetVaccineScheduler.java` dosyasında yer alan bu servis, evcil hayvanların yaş, kilo, aşı geçmişi ve protokol kurallarına bakarak bir sonraki aşı durumunu hesaplar. Servis; kuralların seçimi, doz planlarının uygulanması ve statü üretimi gibi işlemleri adım adım ele alır.

## Girdi Modeli

| Model | Açıklama |
|-------|---------|
| `PetHealthData` | Hayvan türü (`PetType`), doğum tarihi, güncel kilo ve opsiyonel kilo geçmişi (`List<WeightRecord>`) içerir. |
| `VaccineEvent` | Bir aşının tarihini ve varsa `doseNo` bilgisini tutar; aynı gün girilen tekrar kayıtları otomatik tekilleştirilir. |
| Protokol Haritası | `PetType` → `vaccineCode` → `List<VaccineRule>` şeklinde sabitlenmiştir (örnek olarak kedi/köpek protokolleri sınıf içinde tanımlıdır). |

## Rule Şeması

Her `VaccineRule`:

- `when`: `VaccineRuleCondition` ile yaş (min/max gün) ve kilo (min/max kg) aralıklarını belirtir.
- `plan`: `VaccinePlan` türü (ONE_SHOT / SERIES). Seri planında dozu, aralığı ve restart eşiğini, isteğe bağlı booster planını içerir.
- `priority`: Çakışan koşullarda en yüksek öncelikli kural seçilir.

## Algoritma Adımları

1. **Ön hesaplar:** `buildSchedule` fonksiyonu bugünün tarihini, hayvanın yaşını (gün bazında) hesaplar ve aşı geçmişini `vaccineCode` bazında gruplayıp sıralar.
2. **Kural seçimi:** `evaluateVaccine`, hayvan yaş/kilo bilgisi mevcut kuralların koşullarını karşılıyorsa uygun kuralı seçer; aksi halde `NOT_ELIGIBLE` statüsü üretir ve mümkünse bir `dueDate` verir.
3. **Plan değerlendirme:**
   - `evaluateOneShot`: Tek doz veya booster döngüsü olan aşılar için sıradaki tarihe karar verir.
   - `evaluateSeries`: Çok dozlu planlarda güncel dozu, restart gereksinimini veya tamamlanmış/booster durumlarını belirler.
4. **Statü hesaplama:** `determineStatus`, `dueDate` ile bugünü kıyaslayıp `SCHEDULED`, `DUE_SOON`, `OVERDUE`, `NEED_RESTART` gibi sonuçlar üretir.
5. **Çıktı:** Her aşı için `VaccineScheduleResult` döner; bu yapı `vaccineCode`, `status`, `dueDate`, `reason` ve seçilen kural referansını içerir. Sonuç listesi `dueDate` sıralı döner.

## Edge Case Desteği

- Kilo hedefini hesaplayabilmek için `estimateWeightDate` kilo geçmişine bakar; yeterli veri yoksa `NOT_ELIGIBLE` dönebilir.
- Seri planlarında `restartThresholdDays` aşılırsa `NEED_RESTART` statüsü belirir ve plan ilk dozdan başlatılır.
- Booster planlarında ilk ve tekrar rapeller ayrı ayrı hesaplanır.
- Yaş aralığı geçmiş aşılar, `reason = "age window passed"` mesajıyla raporlanır.

## Örnek Kullanım Akışı

1. Controller veya servis, `PetHealthData` ve `VaccineEvent` listesi oluşturur.
2. `PetVaccineScheduler.buildSchedule(pet, history)` çağrısı yapılır.
3. Dönen liste, UI veya API çıktısında her aşı satırı için status/dueDate bilgisi olarak sunulur.

Bu doküman, servis koduna bakmadan bile hangi girdilerin beklendiğini ve algoritmanın hangi durumlarda nasıl sonuç verdiğini hızlıca anlamanızı sağlar.
