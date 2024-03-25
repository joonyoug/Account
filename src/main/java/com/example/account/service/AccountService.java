package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountInfo;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.account.dto.AccountDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository; //내가만든 빈을 다른빈에 넣어주고 싶을떄
    private final AccountUserRepository accountUserRepository;

    /*
        사용자가 있는지 확인 (조회)
        계좌의 번호를 생성하고
        계좌를 저장하고 ,그 정보를 넘긴다.
     */
    @Transactional
    public AccountDto createAccount(Long userID,Long initialBalance){
        AccountUser accountUser = getAccountUser(userID);

        validateCreateAccount(accountUser); //10개 초과시


        String newAccountNumber=accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber()))+1+"")
                .orElse("1000000000");

      return AccountDto.fromEntity(
              accountRepository.save(Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(AccountStatus.IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));


    }

    private AccountUser getAccountUser(Long userID) {
        AccountUser accountUser =accountUserRepository.findById(userID)
                .orElseThrow(()->new AccountException(ErrorCode.USER_NOT_FOUND));
        return accountUser;
    }

    private void validateCreateAccount(AccountUser accountUser){
        if(accountRepository.countByAccountUser(accountUser)==10){
            throw new AccountException(ErrorCode.MAX_COUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id){
        if(id<0){
            throw new RuntimeException("Minus");
        }


        return accountRepository.findById(id).get();
    }
    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);

        Account account= accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND)); //계좌번호가 없는경우

        validateDeleteAccount( accountUser,account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);
        return AccountDto.fromEntity(account);

    }
    private void validateDeleteAccount(AccountUser accountUser,Account account){
        if(!Objects.equals(accountUser.getId(), account.getAccountUser().getId())){
            throw new AccountException(ErrorCode.USER_ACCOUNT_NOT_MATCH);
        }
        if(account.getAccountStatus()==AccountStatus.UNREGISTERED){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance()>0){
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }

    }
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);
        List<Account> accounts=accountRepository.findByAccountUser(accountUser);

        return accounts.stream().map(AccountDto::fromEntity)
                .collect(Collectors.toList());


    }
}
