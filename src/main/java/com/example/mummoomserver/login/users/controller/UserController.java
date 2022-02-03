package com.example.mummoomserver.login.users.controller;

import com.example.mummoomserver.config.resTemplate.ResponseTemplate;
import com.example.mummoomserver.login.jwt.JwtProvider;

import com.example.mummoomserver.login.service.SessionUser;
import com.example.mummoomserver.login.service.UserDetailsImpl;
import com.example.mummoomserver.login.users.Role;
import com.example.mummoomserver.login.users.User;
import com.example.mummoomserver.login.users.UserRepository;
import com.example.mummoomserver.login.users.UserService;
import com.example.mummoomserver.login.users.requestResponse.LoginRequest;
import com.example.mummoomserver.login.users.requestResponse.SignUpRequest;
import com.example.mummoomserver.login.users.requestResponse.UpdateProfileRequest;
import com.example.mummoomserver.login.users.requestResponse.UserProfileResponse;
import com.example.mummoomserver.login.validation.ValidationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final JwtProvider jwtProvider;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final HttpSession httpSession;

    // 회원가입
    @PostMapping("/signup")
    public ResponseTemplate<?> signUpNewUser(@RequestBody @Valid SignUpRequest signUpRequest, BindingResult bindingResult){
        //유효성 검사
        if(bindingResult.hasErrors()) throw new ValidationException("회원가입 유효성 검사 실패.", bindingResult.getFieldErrors());
        //성공 시
        userService.saveUser(signUpRequest);
        return new ResponseTemplate("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public void login(@RequestBody @Valid LoginRequest loginRequest){
        //실패시
        User member = userRepository.findByEmail(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 E-MAIL 입니다."));
        //비밀번호 틀릴 시
        if(!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())){
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }
        // 로그인 성공 시
        // 토큰 발급 쉽게 수정하거나 변경하기
        String token = jwtProvider.createToken(member.getUsername(), member.getRole());
    }


    //프로필 자신 조회 // 권한 문제로 물려있음
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal UserDetailsImpl loginUser) {
        // 자신임을 확인할 수 있는 관환 관련 코드 필요할 듯
        UserProfileResponse.UserProfileResponseBuilder userProfileResponseBuilder = UserProfileResponse.builder()
                .userIdx(loginUser.getUserIdx())
                .nickName(loginUser.getNickName())
                .email(loginUser.getEmail());
                // .authorities(loginUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(s -> AuthorityType.valueOf(s)).collect(Collectors.toList()));
        return ResponseEntity.ok(userProfileResponseBuilder.build());
    }

   // 프로필 수정 사항 반영
    @PutMapping("/me")
    public void updateProfile(@RequestBody @Valid UpdateProfileRequest updateProfileRequest, BindingResult bindingResult, @AuthenticationPrincipal UserDetailsImpl loginUser) {
        if(bindingResult.hasErrors()) throw new ValidationException("프로필 업데이트 유효성 검사 실패", bindingResult.getFieldErrors());
        userService.updateProfile(loginUser.getUsername(), updateProfileRequest);
    }

    // 회원 탈퇴ㅣ?
//    @DeleteMapping("/withdraw")
//    public void withdrawUser(@AuthenticationPrincipal UserDetailsImpl loginUser, HttpServletRequest request, HttpServletResponse response) {
//        Optional<OAuth2AccountDTO> optionalOAuth2AccountDTO = userService.withdrawUser(loginUser.getUsername());
//        //연동된 소셜계정 정보가 있다면 연동해제 요청
//        if(optionalOAuth2AccountDTO.isPresent()) {
//            OAuth2AccountDTO oAuth2AccountDTO = optionalOAuth2AccountDTO.get();
//            ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(oAuth2AccountDTO.getProvider());
//            OAuth2Service oAuth2Service = OAuth2ServiceFactory.getOAuth2Service(restTemplate, oAuth2AccountDTO.getProvider());
//            oAuth2Service.unlink(clientRegistration, oAuth2AccountDTO.getOAuth2Token());
//        }
    //}
}
