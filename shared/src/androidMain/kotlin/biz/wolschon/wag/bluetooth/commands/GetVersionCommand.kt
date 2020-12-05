package biz.wolschon.wag.bluetooth.commands

import androidx.lifecycle.MutableLiveData

actual class GetVersionCommand(
    versionText: MutableLiveData<String>,
    onSuccess: (() -> Unit)? = null
) : AbstractGetVersionCommand(
    reportResult = {version -> versionText.postValue(version)},
    onSuccess = onSuccess
)