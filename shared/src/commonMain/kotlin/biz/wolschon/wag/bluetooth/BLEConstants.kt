
class BLEConstants {
    companion object {
        const val UUID_SERVICE = "927dee04-ddd4-4582-8e42-69dc9fbfae66"

        /**
         * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTConnectionManager.cpp#L37
         */
        //const val UUID_READ_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb"//

        /**
         * "earsCommandReadCharacteristicUuid"
         * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTDeviceEars.cpp#L53
         */
        const val UUID_READ_CHARACTERISTIC ="0b646a19-371e-4327-b169-9632d56c0e84"
        /**
         * "earsCommandWriteCharacteristicUuid"
         * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTDeviceEars.cpp#L53
         */
        const val UUID_WRITE_CHARACTERISTIC = "05e026d8-b395-4416-9f8a-c00d6c3781b9"
    }
}
