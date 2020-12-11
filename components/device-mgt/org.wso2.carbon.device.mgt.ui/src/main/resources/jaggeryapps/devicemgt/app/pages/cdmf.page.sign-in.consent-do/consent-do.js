function onRequest(context) {
    var Encode = Packages.org.owasp.encoder.Encode;
    var viewModel = {};

    // if sp is received, its a saml request or else its oidc
    if(request.getParameter("sp") !== null) {
        viewModel.appName = Encode.forHtml(request.getParameter("sp"));
        viewModel.action = "/commonauth";
        viewModel.sessionDataKey = Encode.forHtmlAttribute(request.getParameter("sessionDataKey"));
        viewModel.sessionDataKeyName = "sessionDataKey";
        viewModel.ssoProtocol = "saml";
    } else {
        viewModel.appName = Encode.forHtml(request.getParameter("application"));
        viewModel.action = "../oauth2/authorize";
        viewModel.sessionDataKey = Encode.forHtmlAttribute(request.getParameter("sessionDataKeyConsent"));
        viewModel.sessionDataKeyName = "sessionDataKeyConsent";
        viewModel.ssoProtocol = "oidc";
    }
    var mandatoryClaims = [];
    var requestedClaims = [];
    var singleMandatoryClaim = false;

    var mandatoryClaimsList, requestedClaimsList;
    var i, j, partOne, partTwo;
    if (request.getParameter("mandatoryClaims")) {
        mandatoryClaimsList = request.getParameter("mandatoryClaims").split(",");
        singleMandatoryClaim = (mandatoryClaimsList.length === 1);
        for (j = 0; j < mandatoryClaimsList.length; j++) {
            var mandatoryClaimsStr = mandatoryClaimsList[j];
            i = mandatoryClaimsStr.indexOf('_');
            partOne = mandatoryClaimsStr.slice(0, i);
            partTwo = mandatoryClaimsStr.slice(i + 1, mandatoryClaimsStr.length);
            mandatoryClaims.push(
                {"claimId": Encode.forHtmlAttribute(partOne), "displayName": Encode.forHtmlAttribute(partTwo)}
            );
        }
    }
    if (request.getParameter("requestedClaims")) {
        requestedClaimsList = request.getParameter("requestedClaims").split(",");
        for (j = 0; j < requestedClaimsList.length; j++) {
            var requestedClaimsStr = requestedClaimsList[j];
            i = requestedClaimsStr.indexOf('_');
            partOne = requestedClaimsStr.slice(0, i);
            partTwo = requestedClaimsStr.slice(i + 1, requestedClaimsStr.length);
            requestedClaims.push(
                {"claimId": Encode.forHtmlAttribute(partOne), "displayName": Encode.forHtmlAttribute(partTwo)}
            );
        }
    }
    viewModel.mandatoryClaims = mandatoryClaims;
    viewModel.requestedClaims = requestedClaims;
    viewModel.singleMandatoryClaim = singleMandatoryClaim;
    return viewModel;
}
