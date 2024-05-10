package io.smallrye.openapi.spring;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.DotName;

/**
 * Constants related to Spring
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SpringConstants {

    static final DotName REST_CONTROLLER = DotName.createSimple("org.springframework.web.bind.annotation.RestController");
    static final DotName REQUEST_MAPPING = DotName.createSimple("org.springframework.web.bind.annotation.RequestMapping");
    static final DotName GET_MAPPING = DotName.createSimple("org.springframework.web.bind.annotation.GetMapping");
    static final DotName PUT_MAPPING = DotName.createSimple("org.springframework.web.bind.annotation.PutMapping");
    static final DotName POST_MAPPING = DotName.createSimple("org.springframework.web.bind.annotation.PostMapping");
    static final DotName DELETE_MAPPING = DotName.createSimple("org.springframework.web.bind.annotation.DeleteMapping");
    // What about HEAD and OPTIONS ?
    static final DotName PATCH_MAPPING = DotName.createSimple("org.springframework.web.bind.annotation.PatchMapping");

    static final DotName REQUEST_BODY = DotName.createSimple("org.springframework.web.bind.annotation.RequestBody");

    static final DotName RESPONSE_ENTITY = DotName.createSimple("org.springframework.http.ResponseEntity");
    static final DotName MUTIPART_FILE = DotName.createSimple("org.springframework.web.multipart.MultipartFile");

    static final DotName QUERY_PARAM = DotName.createSimple("org.springframework.web.bind.annotation.RequestParam");
    static final DotName COOKIE_PARAM = DotName.createSimple("org.springframework.web.bind.annotation.CookieValue");
    static final DotName PATH_PARAM = DotName.createSimple("org.springframework.web.bind.annotation.PathVariable");
    static final DotName HEADER_PARAM = DotName.createSimple("org.springframework.web.bind.annotation.RequestHeader");
    static final DotName MATRIX_PARAM = DotName.createSimple("org.springframework.web.bind.annotation.MatrixVariable");

    static final Set<DotName> CONTEXTS = Stream.of("javax", "jakarta")
            .map(prefix -> DotName.createComponentized(null, prefix))
            .map(prefix -> DotName.createComponentized(prefix, "servlet"))
            .map(prefix -> DotName.createComponentized(prefix, "http"))
            .flatMap(prefix -> Stream.of("HttpServletRequest", "HttpServletResponse", "HttpSession")
                    .map(simpleName -> DotName.createComponentized(prefix, simpleName)))
            .collect(Collectors.toSet());

    public static final Set<DotName> MULTIPART_OUTPUTS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(MUTIPART_FILE)));

    public static final Set<DotName> MULTIPART_INPUTS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(MUTIPART_FILE)));

    public static final Set<DotName> HTTP_METHODS = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(
                    GET_MAPPING,
                    PUT_MAPPING,
                    POST_MAPPING,
                    DELETE_MAPPING,
                    PATCH_MAPPING)));

    public static final String MAPPING_CONSUMES = "consumes";
    public static final String MAPPING_PRODUCES = "produces";

    private SpringConstants() {
    }
}
