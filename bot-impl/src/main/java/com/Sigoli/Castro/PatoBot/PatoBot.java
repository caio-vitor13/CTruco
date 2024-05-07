package com.Sigoli.Castro.PatoBot;

import com.bueno.spi.model.*;
import com.bueno.spi.service.BotServiceProvider;

import java.util.List;
import java.util.Optional;

public class PatoBot implements BotServiceProvider {

    @Override
    public boolean getMaoDeOnzeResponse(GameIntel intel) {
        return checkIfAcceptMaoDeOnze(intel);
    }

    @Override
    public boolean decideIfRaises(GameIntel intel) {
        if (intel.getScore() == 11 || intel.getOpponentScore() == 11) return false;
        if (intel.getRoundResults().isEmpty()) {
            return checkIfRaiseGame(intel);
        } else if (intel.getRoundResults().get(0) == GameIntel.RoundResult.WON) {
            return checkIfRaiseGame(intel);
        }
        return false;
    }

    @Override
    public CardToPlay chooseCard(GameIntel intel) {
        CardToPlay cardToPlay = CardToPlay.of(intel.getCards().get(0));
        if (getNumberOfCardsInHand(intel) == 3 && checkIfOpponentIsFirstToPlay(intel.getOpponentCard())) {
            cardToPlay = CardToPlay.of(attemptToBeatOpponentCard(intel));
        } else if (getNumberOfCardsInHand(intel) == 3 && !checkIfOpponentIsFirstToPlay(intel.getOpponentCard())) {
            cardToPlay = CardToPlay.of(selectStrongerCardExcludingZapAndCopas(intel));
        } else if (getNumberOfCardsInHand(intel) == 2 && !checkIfOpponentIsFirstToPlay(intel.getOpponentCard())) {
            cardToPlay = CardToPlay.of(selectLowestCard(intel.getCards(), intel.getVira()));
        } else if (getNumberOfCardsInHand(intel) == 2 && checkIfOpponentIsFirstToPlay(intel.getOpponentCard())) {
            cardToPlay = CardToPlay.of(attemptToBeatOpponentCard(intel));
        }


        return cardToPlay;
    }

    @Override
    public int getRaiseResponse(GameIntel intel) {
        return checkIfAcceptRaise(intel);
    }


    public Boolean checkIfOpponentIsFirstToPlay(Optional<TrucoCard> opponentCard) {
        return opponentCard.isPresent();
    }

    public int getNumberOfCardsInHand(GameIntel intel) {
        List<TrucoCard> cards = intel.getCards();
        return cards.size();
    }

    public TrucoCard attemptToBeatOpponentCard(GameIntel intel) {
        TrucoCard cardToPlay = null;
        TrucoCard vira = intel.getVira();
        List<TrucoCard> hand = intel.getCards();
        Optional<TrucoCard> opponentCard = intel.getOpponentCard();
        for (TrucoCard card : hand) {
            if (card.compareValueTo(opponentCard.get(), vira) > 0) {
                if (cardToPlay == null || card.compareValueTo(cardToPlay, vira) < 0) {
                    cardToPlay = card;
                }
            }
        }
        if (cardToPlay == null || cardToPlay.compareValueTo(opponentCard.get(), vira) <= 0) {
            cardToPlay = selectLowestCard(hand, vira);
        }
        return cardToPlay;
    }


    public TrucoCard selectLowestCard(List<TrucoCard> hand, TrucoCard vira) {
        TrucoCard lowestCard = null;
        for (TrucoCard card : hand) {
            if (lowestCard == null || card.compareValueTo(lowestCard, vira) < 0) {
                lowestCard = card;
            }
        }
        return lowestCard;
    }

    public TrucoCard selectStrongerCardExcludingZapAndCopas(GameIntel intel) {
//        System.out.println(intel);
//        System.out.println(intel.getCards());
        List<TrucoCard> hand = intel.getCards();
        TrucoCard vira = intel.getVira();
        TrucoCard strongestCard = intel.getCards().get(0);

        for (TrucoCard card : hand) {
            if (!card.isZap(vira) && !card.isCopas(vira)) {
                if (card.relativeValue(vira) > strongestCard.relativeValue(vira)) {
                    strongestCard = card;
                }
            }
        }
        return strongestCard;
    }

    public boolean checkIfAcceptMaoDeOnze(GameIntel intel) {
        int count = 0;
        for (TrucoCard card : intel.getCards()) {
            if (card.isManilha(intel.getVira())) {
                count += 3;
            }
            if (card.compareValueTo(TrucoCard.of(CardRank.THREE, CardSuit.HEARTS), intel.getVira()) >= 0) {
                count++;
            }
        }
        int opponentPoints = intel.getOpponentScore();
        int threshold = 4;
        if (opponentPoints >= 8) {
            threshold = 6;
        }
        return count >= threshold;
    }

    public boolean checkIfStrongerCardIsThree(GameIntel intel) {
        TrucoCard ThreeToCompare = TrucoCard.of(CardRank.THREE, CardSuit.DIAMONDS);
        TrucoCard testCard = selectStrongerCardExcludingZapAndCopas(intel);
        return !testCard.isManilha(intel.getVira()) && testCard.compareValueTo(ThreeToCompare, intel.getVira()) == 0;

    }

    public boolean checkIfRaiseGame(GameIntel intel) {
        int count = 0;
        TrucoCard vira = intel.getVira();
        TrucoCard CardToCompare = TrucoCard.of(CardRank.TWO, CardSuit.DIAMONDS);
        List<TrucoCard> cards = intel.getCards();
        for (TrucoCard card : cards) {
            if (card.compareValueTo(CardToCompare, vira) >= 0 || card.isManilha(vira)) {
                count++;
            }
        }
        if (checkIfStrongerCardIsThree(intel)) {
            count--;
        }

        if (cards.size() == 3) {
            return count >= 2;
        }
        return count >= 1;
    }

    public int checkIfAcceptRaise(GameIntel intel) {
        List<TrucoCard> hand = intel.getCards();
        TrucoCard vira = intel.getVira();
        TrucoCard CardToCompare = TrucoCard.of(CardRank.TWO, CardSuit.DIAMONDS);
        List<GameIntel.RoundResult> roundResults = intel.getRoundResults();
        int score = 0;

        if (hand.size() == 3) {
            return 1;
        }
        if (!hand.isEmpty()) {
            for (TrucoCard card : hand) {
                if (card.isManilha(vira)) {
                    score += 3;
                } else if (card.compareValueTo(CardToCompare, vira) >= 0) {
                    score++;
                }
            }
            if (checkIfStrongerCardIsThree(intel)) {
                score--;
            }
        }

        if (!roundResults.isEmpty()) {
            if (roundResults.contains(GameIntel.RoundResult.WON)) {
                if (score >= 3 && checkIfHasZap(hand, vira)) {
                    return 1;
                } else if (score >= 4 && checkIfHasManilha(hand, vira)) {
                    return 0;
                } else if (score == 3) {
                    return 0;
                } else {
                    return -1;
                }
            } else {
                if (score >= 4 && (checkIfHasZap(hand, vira) || checkIfHasCopas(hand, vira))) {
                    return 0;
                } else {
                    return -1;
                }
            }
        }
        return -1;
    }

    public boolean checkIfHasZap(List<TrucoCard> hand, TrucoCard vira) {
        for (TrucoCard card : hand) {
            if (card.isZap(vira)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfHasCopas(List<TrucoCard> hand, TrucoCard vira) {
        for (TrucoCard card : hand) {
            if (card.isCopas(vira)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfHasManilha(List<TrucoCard> hand, TrucoCard vira) {
        for (TrucoCard card : hand) {
            if (card.isManilha(vira)) {
                return true;
            }
        }
        return false;
    }
}