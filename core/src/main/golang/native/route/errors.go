package route

var (
	ErrUnauthorized   = newError("UnAuthorized")
	ErrBadRequest     = newError("Body invalid")
	ErrForbidden      = newError("Forbidden")
	ErrNotFound       = newError("Resource not found")
	ErrRequestTimeout = newError("Timeout")
)

type HTTPError struct {
	Message string `json:"message"`
}

func (e *HTTPError) Error() string {
	return e.Message
}

func newError(msg string) *HTTPError {
	return &HTTPError{Message: msg}
}
