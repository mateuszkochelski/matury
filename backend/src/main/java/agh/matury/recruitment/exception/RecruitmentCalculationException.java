package agh.matury.recruitment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RecruitmentCalculationException extends RuntimeException {

    public RecruitmentCalculationException(String message) {
        super(message);
    }
}
