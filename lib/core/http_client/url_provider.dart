class UrlProvider {
  final String _baseAuthority = "http://localhost:11434";
  final String _baseEndpoint = "/api";

  Uri forPath(String endpoint){
    return Uri.parse("$_baseAuthority$_baseEndpoint$endpoint");
  }
}
