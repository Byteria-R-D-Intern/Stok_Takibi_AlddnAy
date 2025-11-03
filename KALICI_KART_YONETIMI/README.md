# Kalıcı Kart Yönetimi (Saved Payment Methods)

Bu klasör, projeye DAHİL OLMAYAN örnek Java dosyalarını içerir. İleride kalıcı kart yöntemlerini eklemek istersen aşağıdaki dosyaları `demo/src/main/java` altındaki uygun paketlere taşıyıp uygulayabilirsin. Şu an derlenmezler.

İçerik:
- `CardSealingService.java`: PAN+SKT verisini AES‑GCM ile mühürleme/açma.
- `SavedPaymentMethod.java`: JPA entity.
- `SavedPaymentMethodRepository.java`: Domain port.
- `SavedPaymentMethodJpaRepository.java`: Spring Data JPA repo.
- `SavedPaymentMethodPersistenceAdapter.java`: Port implementasyonu.
- `UserPaymentMethodController.java`: Kullanıcıya ait yöntemleri listele/ekle/sil (JWT gerekli).
- `MIGRATION_postgres.sql`: Örnek PostgreSQL şema/migrasyon komutları.

Notlar:
- Üretimde PAN saklamak yerine PSP token (card-on-file) saklamayı tercih et. Bu örnek eğitim amaçlıdır.
- Anahtar yönetimi için KMS/HSM kullan (ör. AWS KMS) ve anahtar rotasyonu planla.


