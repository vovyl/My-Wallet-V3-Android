package com.blockchain.metadata

interface MetadataWarningLog {

    /**
     * Log a warning about metadata. Do NOT include public or private information in the log.
     */
    fun logWarning(warning: String)
}
