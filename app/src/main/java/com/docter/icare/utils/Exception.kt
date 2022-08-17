package com.docter.icare.utils

class InputException(message: String) : Exception(message)

class NoInternetException(message: String) : Exception(message)

class ApiConnectFailException(message: String) : Exception(message)

class SidException(message: String) : Exception(message)

class SocketAbortedException : Exception()