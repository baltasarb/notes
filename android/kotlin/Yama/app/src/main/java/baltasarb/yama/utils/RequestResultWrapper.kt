package baltasarb.yama.utils

class RequestResultWrapper<T> {

    var dataArray: Array<T>? = null
    var data: T? = null

    constructor(data: T) {
        this.data = data
    }

    constructor(dataArray: Array<T>) {
        this.dataArray = dataArray
    }
}