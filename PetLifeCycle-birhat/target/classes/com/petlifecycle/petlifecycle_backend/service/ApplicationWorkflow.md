# Application Workflow Motoru

Bu doküman, `ApplicationWorkflowService` sınıfının (src/main/java/com/petlifecycle/petlifecycle_backend/service/ApplicationWorkflowService.java) yönettiği sahiplenme başvurusu durum makinesinin kurallarını özetler.

## Durumlar

| State | Açıklama |
|-------|---------|
| `DRAFT` | Başvuru taslak halinde, henüz gönderilmedi. |
| `SUBMITTED` | Başvuru gönderildi, inceleme bekliyor. |
| `UNDER_REVIEW` | Ekip inceleme aşamasında. |
| `WAITING_INFO` | Ek bilgi/evrak bekleniyor. |
| `APPROVED` | Başvuru onaylandı. |
| `REJECTED` | Başvuru reddedildi. |
| `CANCELLED` | Başvuru sahibi tarafından iptal edildi. |
| `EXPIRED` | Süre doldu, başvuru iptal. |

Terminal durumlar: `APPROVED`, `REJECTED`, `CANCELLED`, `EXPIRED`.

## Eventler

`ApplicationEventType` enumu aşağıdaki tetikleyicileri içerir:

`SAVE_DRAFT`, `SUBMIT_APPLICATION`, `START_REVIEW`, `REQUEST_INFO`, `PROVIDE_INFO`, `APPROVE`, `REJECT`, `CANCEL`, `EXPIRE`

Her event, `ApplicationEventCommand` kaydı ile aktarılır: `eventType`, `actorId`, `actorRole`, `payload` (Map) ve `idempotencyKey`.

## Geçiş Tablosu (Allowed Transitions)

| Mevcut state | Event | Yeni state |
|--------------|-------|------------|
| `DRAFT` | `SUBMIT_APPLICATION` | `SUBMITTED` |
| `DRAFT` | `CANCEL` | `CANCELLED` |
| `SUBMITTED` | `START_REVIEW` | `UNDER_REVIEW` |
| `SUBMITTED` | `CANCEL` | `CANCELLED` |
| `SUBMITTED` | `EXPIRE` | `EXPIRED` |
| `UNDER_REVIEW` | `REQUEST_INFO` | `WAITING_INFO` |
| `UNDER_REVIEW` | `APPROVE` | `APPROVED` |
| `UNDER_REVIEW` | `REJECT` | `REJECTED` |
| `UNDER_REVIEW` | `CANCEL` | `CANCELLED` |
| `WAITING_INFO` | `PROVIDE_INFO` | `UNDER_REVIEW` |
| `WAITING_INFO` | `CANCEL` | `CANCELLED` |
| `WAITING_INFO` | `EXPIRE` | `EXPIRED` |

Bu tablonun dışında kalan kombinasyonlar `INVALID_TRANSITION` hata kodu ile reddedilir.

## Guard Koşulları

`evaluateGuards` fonksiyonu her event için ön şartı doğrular:

- `SUBMIT_APPLICATION`: Kullanıcı, pet atanmış olmalı ve payload'da `requiredFieldsComplete=true`.
- `START_REVIEW`: `actorRole` REVIEWER/ADMIN olmalı.
- `REQUEST_INFO`: payload'da `requestedInfo` içeriği boş olmamalı.
- `PROVIDE_INFO`: payload `attachmentsUploaded=true`.
- `APPROVE`: `checklistComplete=true` ve `petAvailable=true`.
- `REJECT`: payload `reason` zorunlu.
- `CANCEL`: İsteğe bağlı `reason`.
- `EXPIRE`: `ttlExpired=true`.
- `SAVE_DRAFT`: Guard yok.

Guard başarısızsa `TransitionResult.denied(..., reasonCode)` döner ve state değişmez.

## Side Effectler

`applySideEffects` metodu, event sonrası Application üzerinde gerekli alanları günceller:

- `SUBMIT_APPLICATION`: `submittedAt` zamanını set eder.
- `START_REVIEW`: `reviewerId` doldurulur.
- `REQUEST_INFO`: `requestedInfoDetails` + `waitingInfoSince`.
- `PROVIDE_INFO`: `waitingInfoSince` temizlenir.
- `APPROVE`: `rejectionReason` null'lanır (pet hold işlemleri ileride eklenecek).
- `REJECT`: `rejectionReason` doldurulur.
- `CANCEL`: `cancelReason` doldurulur.
- `EXPIRE`: `waitingInfoSince` temizlenir.

Her geçiş için `ApplicationTransitionLog` kaydı oluşturulur (actor bilgiler, reasonCode, notes, idempotency key).

## İdempotency

`Application` üzerinde `lastIdempotencyKey` ve `lastIdempotentState` alanları tutulur. Aynı `idempotencyKey` ile gelen tekrar çağrıları `fromCache=true` flag’i ile mevcut sonucu döner; böylece API retriable olur.

## Testler

`ApplicationWorkflowServiceTest` sınıfı (JUnit + Mockito) başarılı bir SUBMIT → REVIEW → APPROVE akışını ve guard reddini doğrular. Maven testleri (`./mvnw test`) bu kapsamı çalıştırır.

## Admin API

- `GET /api/admin/applications/pending`: SUBMITTED/UNDER_REVIEW/WAITING_INFO durumundaki başvuruları listeler.
- `POST /api/admin/applications/{id}/approve`: Admin kimliği, checklist/pet uygunluğu ve idempotency anahtarıyla onay verir.
- `POST /api/admin/applications/{id}/reject`: Admin kimliği ve reddetme gerekçesi ile başvuruyu reddeder.

Bu uç noktalar `AdminApplicationController` ve `AdminApplicationService` üzerinden workflow motorunu tetikler.
