# AFIP SDK

This library provides easy access to the [AFIP](https://www.afip.gob.ar) web services.

The current version supports the following services:

* Electronic tickets ([wsfe](https://www.afip.gob.ar/fe/ayuda//documentos/Manual-desarrollador-V.2.21.pdf))
* Export electronic tickets ([wsfex](https://www.afip.gob.ar/fe/documentos/WSFEX-Manualparaeldesarrollador_V1_9.pdf))

## Getting started

You need to initialize a ServiceProvider that will give you access to supported services.

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
}
```

The SDK uses a general purpose [ObjectStore](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/store/ObjectStore.kt)
to cache credentials and save the secrets. The `store { }` block in the ServiceProvider builder allows configuring
the ObjectStore implementation. This SDK provides two object stores out of the box: a
[FileSystemObject](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/store/FileSystemObjectStore.kt) store and
a [MemoryObjectStore](https://github.com/f-nyx/afip-sdk-kotlin/blob/main/src/main/kotlin/be/rlab/afip/support/store/InMemoryObjectStore.kt).
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

Look at the [Electronic Ticket](electronic-ticket) section for further information.

## Authentication

TBD

## Electronic Ticket

TBD
