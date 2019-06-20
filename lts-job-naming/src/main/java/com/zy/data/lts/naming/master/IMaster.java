package com.zy.data.lts.naming.master;

import com.zy.data.lts.core.TriggerMode;
import com.zy.data.lts.core.api.IAdminApi;

public interface IMaster extends IAdminApi {

    void trigger(int flowId, TriggerMode triggerMode, String params);
}
