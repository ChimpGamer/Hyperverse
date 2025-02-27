//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package org.incendo.hyperverse.flags.implementation;

import org.bukkit.Difficulty;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.FlagParseException;
import org.incendo.hyperverse.flags.WorldFlag;

import java.util.Arrays;
import java.util.Collection;

public final class DifficultyFlag extends WorldFlag<Difficulty, DifficultyFlag> {

    public static final DifficultyFlag DIFFICULTY_FLAG_PEACEFUL = new DifficultyFlag(Difficulty.PEACEFUL);
    public static final DifficultyFlag DIFFICULTY_FLAG_EASY = new DifficultyFlag(Difficulty.EASY);
    public static final DifficultyFlag DIFFICULTY_FLAG_NORMAL = new DifficultyFlag(Difficulty.NORMAL);
    public static final DifficultyFlag DIFFICULTY_FLAG_HARD = new DifficultyFlag(Difficulty.HARD);

    private DifficultyFlag(final @NonNull Difficulty difficulty) {
        super(difficulty, Messages.flagDescriptionDifficulty);
    }

    @Override
    public DifficultyFlag parse(final @NonNull String input) throws FlagParseException {
        return switch (input.toLowerCase()) {
            case "peaceful" -> this.flagOf(Difficulty.PEACEFUL);
            case "easy" -> this.flagOf(Difficulty.EASY);
            case "normal" -> this.flagOf(Difficulty.NORMAL);
            case "hard" -> this.flagOf(Difficulty.HARD);
            default -> throw new FlagParseException(
                    this, input,
                    "Invalid difficulty. Available values are: peaceful, easy, normal and hard"
            );
        };
    }

    @Override
    public DifficultyFlag merge(final @NonNull Difficulty newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue().name().toLowerCase();
    }

    @Override
    public String getExample() {
        return "peaceful";
    }

    @Override
    protected DifficultyFlag flagOf(final @NonNull Difficulty value) {
        return switch (value) {
            case PEACEFUL -> DIFFICULTY_FLAG_PEACEFUL;
            case EASY -> DIFFICULTY_FLAG_EASY;
            case HARD -> DIFFICULTY_FLAG_HARD;
            default -> DIFFICULTY_FLAG_NORMAL;
        };
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays.asList("peaceful", "easy", "normal", "hard");
    }

}
