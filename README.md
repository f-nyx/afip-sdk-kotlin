# AFIP SDK

This library provides easy access to the [AFIP](https://www.afip.gob.ar) applications and web services.

The current version supports the following services:

* Electronic tickets ([wsfe](https://www.afip.gob.ar/fe/ayuda//documentos/Manual-desarrollador-V.2.21.pdf))
* Export electronic tickets ([wsfex](https://www.afip.gob.ar/fe/documentos/WSFEX-Manualparaeldesarrollador_V1_9.pdf))
* Certificate manager for testing ([wsass](https://wsass-homo.afip.gob.ar/wsass/portal/main.aspx))

## Getting started

This library is available in Maven Central. Add this dependency to your project:

```xml
<dependency>
    <groupId>be.rlab.afip</groupId>
    <artifactId>afip-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then you need to initialize a ServiceProvider that will give you access to the supported services.

```kotlin
companion object {
    const val STORE_DIR: String = "/path/to/store"
    const val KEY_STORE_FILE: String = "/path/keystore.p12"
    const val CUIT: Long = 20304050603L
    const val ALIAS: String = "afiptest"
    const val PASSWORD: String = "Test123"
}

val serviceProvider = ServiceProvider.new {
    store {
        fileSystem { storeDir = File(STORE_DIR) }
    }
    secretsProvider {
        cuit = CUIT
        alias = ALIAS
        password = PASSWORD
        fileSystem { keyStoreFile = File(KEY_STORE_FILE) }
    }
    portal {
        cuit = CUIT
        password = PASSWORD
    }
}
```

The SDK uses a general purpose [ObjectStore](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/store/ObjectStore.kt)
to cache credentials and save the secrets. The `store { }` block in the ServiceProvider builder configures
the ObjectStore implementation. This SDK provides two object stores out of the box: 
[FileSystemObjectStore](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/store/FileSystemObjectStore.kt) and
[MemoryObjectStore](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/store/InMemoryObjectStore.kt).
The `FileSystemObjectStore` uses a directory to store data serialized as JSON.

The `secretsProvider { }` block configures the certificate required to authenticate the AFIP services. Both
the certificate issued by the AFIP and the private key must be saved as a PKCS12 keystore. Look at the
[Authentication](#authentication) section below for further information. In this block you need to specify your
CUIT, the certificate and private key alias in the keystore, the keystore password, and the key store data source.
In this example, we read the keystore from the file system.

Once you configure the service provider, you can use any of the supported services:

```kotlin
val ticketService: TicketService = serviceProvider.getService()
println(ticketService.getCurrencyTypes())
```

Look at the [Electronic Ticket](#electronic-ticket) section for further information.

## Authentication

The authentication to AFIP services requires a certificate issued by the AFIP. The certificate is used to sign the
SOAP requests. A cryptographic signature allows to identify a subject. The certificate has information
associated to a person or entity (subject) like the CUIT, application name, and company name. So, signing a request
allows the services to verify that it comes from a single real subject. This section assumes you are familiar
with certificates, if not, please take a look at [how certificates work](#how-certificates-work) first.

In our case the AFIP is the Certificate Authority. So, we need to generate the CSR required by the AFIP to issue
the certificate. The AFIP provides two environments: testing (homologacion) and production (produccion). You need to
generate a certificate for each environment. You can use the same CSR to generate both certificates, but we strongly
recommend having a single public/private key pair and CSR for each environment.

This library works with a key store containing both the public/private key pair and the certificate. The following
steps describe the process to create the required objects:

1. Create the public/private key pair
2. Create the CSR required by the AFIP to issue the certificate
3. Generate the certificate in the AFIP system using the CSR
4. Save the key pair and the certificate in a key store

This SDK comes with a
[Certificate Manager](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/CertificateManager.kt)
component that creates and saves all the required objects. You can take a look at
[this test file](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/test/kotlin/be/rlab/afip/support/CertificateManagerTest.kt).
It has two tests: one to create they key pair and the CSR, and another to load the generated certificate and save it
into the Certificate Manager. You just need to configure your information in the constants defined in the companion
object.

Once loaded into the Certificate Manager, you can build the Service Provider using this component. Take into
account that the key store name must be the same you used in the test. The file system store directory must be
also the same directory you configured in the test.

```kotlin
secretsProvider {
    cuit = CUIT
    alias = ALIAS
    password = PASSWORD
    certificateManager { keyStoreName = "afip.keyStore" }
}
```

## Electronic Ticket

The [TicketService](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/ticket/TicketService.kt)
provides access to both
[wsfe](https://www.afip.gob.ar/fe/ayuda//documentos/Manual-desarrollador-V.2.21.pdf) and
[wsfex](https://www.afip.gob.ar/fe/documentos/WSFEX-Manualparaeldesarrollador_V1_9.pdf) AFIP services. You can get a
reference to the service via the Service Provider:

```kotlin
val ticketService: TicketService = serviceProvider.getService()
```

The following sections will describe the supported operations. You can also take a look at the 
[service integration test](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/test/kotlin/be/rlab/afip/ticket/TicketServiceIntegrationTest.kt).

### Create Ticket C

Create a ticket of type C for a service:

```kotlin
val now = DateTime.now()
val pointOfSale: Int = 3

// act
val ticket: Ticket<TicketItem> = ticketService.newTicketC(pointOfSale) {
    addServiceItem(
        totalValue = 2000.0,
        startDate = now.minusDays(15),
        endDate = now.plusDays(15),
        paymentDueDate = now.plusDays(25)
    )
}

println(ticket)
```

Create ticket of type E for an export service:

```kotlin
val pointOfSale: Int = 3

val ticket: Ticket<ExportTicketItem> = ticketService.newTicketE(pointOfSale) {
    exportRecipient(
        customerFullName = "John Smith",
        customerFullAddress = "P Sherman Calle Wallaby 235",
        customerLegalId = "12345678",
        customerType = EntityType.INDIVIDUAL,
        targetLocation = Location.UnitedStates,
        language = Language.English,
        paymentMethod = "Wire Transfer"
    )
    addExportServiceItem(
        code = "ABC123",
        description = "Software Development Services",
        totalValue = 1000.0
    )
}

println(ticket)
```

## How certificates work

As we mentioned earlier in this document, a cryptographic signature allows to identify a subject. Certificates
are used to **sign** data so any entity can verify that the data comes from the subject that claims ownership.

**The certificates have a hierarchy**. A certificate X can be used to _sign_ another certificate Y. It means
the certificate X will validate the Y authenticity. This hierarchy of verified certificates is usually call a
_certificate chain_.

The entity that owns the certificate X used to sign the certificate Y is called **Certificate Authority or CA**. We
say that the CA _issues_ a certificate when it creates the certificate Y signed with X. A certificate contains a
subject identity (i.e.: name, email, company name, etc.). Signing a certificate means certifying the identity and the
ownership of a public key by the subject of the certificate Y.

The CA needs a [Certificate Signing Request (CSR)](https://en.wikipedia.org/wiki/Certificate_signing_request) to
issue a certificate. The CSR usually contains the public key for which the certificate should be issued, identifying
information (such as a domain name) and integrity protection (e.g., a digital signature). This is all the information
the CA needs to create the certificate Y.

The last piece that we need is a public/private key pairs. This project uses RSA keys of 4096 bits. The keys are
used in several operations to sign and verify data. For instance, the private key is used to sign the CSR, so the CA
can verify the signature using the public key within the CSR.

Finally, to put everything together, we need to store the certificate and the public/private keys in a **key store**.
A key store supports securely saving cryptographic objects. It is possible to set a password to decrypt the file
content or even retrieve an object. This project supports the standard PFX/PKCS#12 key store format.
