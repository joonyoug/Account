package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import org.assertj.core.api.BDDAssumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.BDDAssumptions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;


    @Test
    void creatAccountSuccess() {
        //given
        AccountUser accountUser=AccountUser.builder()

                .name("Pibi")
                .build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                                .accountUser(accountUser)
                                .accountNumber("1000000012")
                        .build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013")
                        .build()
                );

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto=accountService.createAccount(1L,1000L);


        //then
        verify(accountRepository,times(1)).save(captor.capture());


        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000013",captor.getValue().getAccountNumber());

    }
    @Test
    void creatFirstAccount() {
        //given
        AccountUser accountUser=AccountUser.builder()

                .name("Pibi")
                .build();
        accountUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013")
                        .build()
                );
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto=accountService.createAccount(1L,1000L);


        //then
        verify(accountRepository,times(1)).save(captor.capture());


        assertEquals(12L,accountDto.getUserId());
        assertEquals("1000000000",captor.getValue().getAccountNumber());

    }


    @Test
    @DisplayName("해당 유저 없음 -계좌 생성 실패 ")
    void createAccountUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException exception=
                assertThrows(AccountException.class,()->
                        accountService.createAccount(1L,1000L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCodes());


    }
    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser accountUser=AccountUser.builder()
                .name("pobi")

                .build();
        accountUser.setId(1L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        //when
        AccountException exception =assertThrows(AccountException.class ,
                ()-> accountService.createAccount(1L,1000L));

        //then
        assertEquals(ErrorCode.MAX_COUNT_PER_USER_10,exception.getErrorCodes());
    }
    @Test
    void deleteAccountSuccess() {
        //given
        AccountUser accountUser=AccountUser.builder()

                .name("pobi")
                .build();
        accountUser.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountNumber("10000000012")
                        .build()));
        ArgumentCaptor<Account> captor=ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto=accountService.deleteAccount(1L,"1234567890");


        //then
        verify(accountRepository,times(1)).save(captor.capture());
        assertEquals(12L,accountDto.getUserId());
        assertEquals("10000000012",captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED,captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccount_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception=assertThrows(AccountException.class,()->
                accountService.deleteAccount(1L,"1234567890"));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND,exception.getErrorCodes());
    }
    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccount_AccountNotFound() {
        //given
        AccountUser accountUser=AccountUser.builder()

                .name("pobi")
                .build();
        accountUser.setId(1L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException exception=assertThrows(AccountException.class,
                ()->accountService.deleteAccount(1L,"1234567890"));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND,exception.getErrorCodes());

    }
    @Test
    @DisplayName("계좌 소유주 다름")
    void deleteAccountFailed_userMatch() {
        //given
        AccountUser accountUser=AccountUser.builder()

                .name("pobi")
                .build();
        accountUser.setId(1L);

        AccountUser otherUser=AccountUser.builder()

                .name("mimi")
                .build();
        otherUser.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));


        //when
        AccountException exception= assertThrows(AccountException.class,()->
                accountService.deleteAccount(1L,"1234567890"));
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_NOT_MATCH,exception.getErrorCodes());
    }
    @Test
    @DisplayName("해지 계좌는 잔액이 없어야 한다.")
    void deleteAccountFailed_BalanceNotEmpty() {
        //given
        AccountUser accountUser= AccountUser.builder()

                .name("pobi")
                .build();
        accountUser.setId(1L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(100L)
                        .accountNumber("1000000012")
                        .build()));
        //when
        AccountException exception= assertThrows(AccountException.class,()->
                accountService.deleteAccount(1L,"1234567890"));
        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY,exception.getErrorCodes());
    }
    @Test
    @DisplayName("해지 계좌는 해지할 수 없다.")
    void deleteAccountFailed_alreadyUnregistered() {
        //given
        AccountUser accountUser= AccountUser.builder()

                .name("pobi")
                .build();
        accountUser.setId(1L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("1000000012")
                        .build()));
        //when
        AccountException exception= assertThrows(AccountException.class,()->
                accountService.deleteAccount(1L,"1234567890"));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED,exception.getErrorCodes());
    }


    @Test
    void successGetAccountsByUserId() {
        //given
        AccountUser accountUser=AccountUser.builder()
                .name("pobi")

                .build();
        accountUser.setId(1L);

        List<Account> accounts= Arrays.asList(
                Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                       .accountUser(accountUser)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);


        //when
        List<AccountDto> accountDtos=accountService.getAccountsByUserId(1L);


        //then
        assertEquals(3,accountDtos.size());
        assertEquals(accountDtos.get(0).getAccountNumber(),"1111111111");
        assertEquals(accountDtos.get(0).getBalance(),1000);
        assertEquals(accountDtos.get(1).getAccountNumber(),"2222222222");
        assertEquals(accountDtos.get(1).getBalance(),2000);
        assertEquals(accountDtos.get(2).getAccountNumber(),"3333333333");
        assertEquals(accountDtos.get(2).getBalance(),3000);


    }
    @Test
    void failedToGetAccounts() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception=assertThrows(AccountException.class,()->
                accountService.getAccountsByUserId(1L));

        //then

        assertEquals(exception.getErrorCodes(),ErrorCode.USER_NOT_FOUND);


    }
















}