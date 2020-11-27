class BLEConstants {
    companion object {

        /**
         * All EarGear have this as their Bluetooth device name.
         */
        const val NAME_EARGEAR = "EarGear"

        /**
         * All DigiTail have this as their Bluetooth device name.
         */
        const val NAME_DIGITAIL = "(!)Tail1"

        fun getServiceUUID(deviceName: CharSequence) =
            when (deviceName) {
                NAME_DIGITAIL -> UUID_SERVICE_DIGITAIL
                else -> UUID_SERVICE_EARGEAR
            }

        const val UUID_SERVICE_DIGITAIL = "000" + "FFE0" + "-0000-1000-8000-00805F9B34FB"
        const val UUID_SERVICE_EARGEAR = "927dee04-ddd4-4582-8e42-69dc9fbfae66"

        /**
         * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTConnectionManager.cpp#L37
         */
        //const val UUID_READ_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb"//

        fun getReadCharacteristicUUID(deviceName: CharSequence) =
            when (deviceName) {
                NAME_DIGITAIL -> UUID_READ_CHARACTERISTIC_DIGITAIL
                else -> UUID_READ_CHARACTERISTIC_EARGEAR
            }

        /**
         * "earsCommandReadCharacteristicUuid"
         * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTDeviceEars.cpp#L53
         */
        const val UUID_READ_CHARACTERISTIC_EARGEAR = "0b646a19-371e-4327-b169-9632d56c0e84"
        const val UUID_READ_CHARACTERISTIC_DIGITAIL = "0000ffe1-0000-1000-8000-00805f9b34fb"

        fun getWriteCharacteristicUUID(deviceName: CharSequence) =
            when (deviceName) {
                NAME_DIGITAIL -> UUID_WRITE_CHARACTERISTIC_DIGITAIL
                else -> UUID_WRITE_CHARACTERISTIC_EARGEAR
            }

        /**
         * "earsCommandWriteCharacteristicUuid"
         * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTDeviceEars.cpp#L53
         */
        const val UUID_WRITE_CHARACTERISTIC_EARGEAR = "05e026d8-b395-4416-9f8a-c00d6c3781b9"
        const val UUID_WRITE_CHARACTERISTIC_DIGITAIL = "0000ffe1-0000-1000-8000-00805f9b34fb"
    }
}
