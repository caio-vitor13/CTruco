/*
 *  Copyright (C) 2021 Lucas B. R. de Oliveira - IFSP/SCL
 *  Contact: lucas <dot> oliveira <at> ifsp <dot> edu <dot> br
 *
 *  This file is part of CTruco (Truco game for didactic purpose).
 *
 *  CTruco is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  CTruco is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CTruco.  If not, see <https://www.gnu.org/licenses/>
 */

package com.bueno.domain.usecases.game;

import com.bueno.domain.entities.game.Game;
import com.bueno.domain.entities.game.Hand;
import com.bueno.domain.entities.game.Intel;
import com.bueno.domain.entities.player.util.Player;
import com.bueno.domain.usecases.hand.PlayCardUseCase;

public class PlayGameUseCase {

    private final Game game;

    public PlayGameUseCase(Player player1, Player player2){
        game = new Game(player1, player2);
    }

    public Intel playNewHand(){
        Intel intel = null;

        if(game.getWinner().isEmpty()) {
            final Hand playedHand = new PlayCardUseCase(game).play();
            intel = playedHand.getIntelOLD();
            game.updateScores();
        }
        return intel;
    }
}