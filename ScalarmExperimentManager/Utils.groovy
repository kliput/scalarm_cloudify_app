static def isUrlAvailable(url) {
    def output = ["sh", "-c", "curl -k -I ${url} | head -1 | cut -d' ' -f2"].execute().text
    output.isInteger() && output.toInteger() in [200, 301]
}
