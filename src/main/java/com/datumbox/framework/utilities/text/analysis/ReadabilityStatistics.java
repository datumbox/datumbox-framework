/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.framework.utilities.text.analysis;

import com.datumbox.common.utilities.PHPfunctions;
import com.datumbox.framework.utilities.text.cleaners.HTMLCleaner;
import com.datumbox.framework.utilities.text.cleaners.StringCleaner;
import com.datumbox.framework.utilities.text.tokenizers.WhitespaceTokenizer;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class ReadabilityStatistics {
    
    protected static Set<String> arrDaleChallWordList = new HashSet<>(Arrays.asList("a", "able", "aboard", "about", "above", "absent", "accept", "accident", "account", "ache", "aching", "acorn", "acre", "across", "act", "acts", "add", "address", "admire", "adventure", "afar", "afraid", "after", "afternoon", "afterward", "afterwards", "again", "against", "age", "aged", "ago", "agree", "ah", "ahead", "aid", "aim", "air", "airfield", "airplane", "airport", "airship", "airy", "alarm", "alike", "alive", "all", "alley", "alligator", "allow", "almost", "alone", "along", "aloud", "already", "also", "always", "am", "America", "American", "among", "amount", "an", "and", "angel", "anger", "angry", "animal", "another", "answer", "ant", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anywhere", "apart", "apartment", "ape", "apiece", "appear", "apple", "April", "apron", "are", "aren\'t", "arise", "arithmetic", "arm", "armful", "army", "arose", "around", "arrange", "arrive", "arrived", "arrow", "art", "artist", "as", "ash", "ashes", "aside", "ask", "asleep", "at", "ate", "attack", "attend", "attention", "August", "aunt", "author", "auto", "automobile", "autumn", "avenue", "awake", "awaken", "away", "awful", "awfully", "awhile", "ax", "axe", "baa", "babe", "babies", "back", "background", "backward", "backwards", "bacon", "bad", "badge", "badly", "bag", "bake", "baker", "bakery", "baking", "ball", "balloon", "banana", "band", "bandage", "bang", "banjo", "bank", "banker", "bar", "barber", "bare", "barefoot", "barely", "bark", "barn", "barrel", "base", "baseball", "basement", "basket", "bat", "batch", "bath", "bathe", "bathing", "bathroom", "bathtub", "battle", "battleship", "bay", "be", "beach", "bead", "beam", "bean", "bear", "beard", "beast", "beat", "beating", "beautiful", "beautify", "beauty", "became", "because", "become", "becoming", "bed", "bedbug", "bedroom", "bedspread", "bedtime", "bee", "beech", "beef", "beefsteak", "beehive", "been", "beer", "beet", "before", "beg", "began", "beggar", "begged", "begin", "beginning", "begun", "behave", "behind", "being", "believe", "bell", "belong", "below", "belt", "bench", "bend", "beneath", "bent", "berries", "berry", "beside", "besides", "best", "bet", "better", "between", "bib", "bible", "bicycle", "bid", "big", "bigger", "bill", "billboard", "bin", "bind", "bird", "birth", "birthday", "biscuit", "bit", "bite", "biting", "bitter", "black", "blackberry", "blackbird", "blackboard", "blackness", "blacksmith", "blame", "blank", "blanket", "blast", "blaze", "bleed", "bless", "blessing", "blew", "blind", "blindfold", "blinds", "block", "blood", "bloom", "blossom", "blot", "blow", "blue", "blueberry", "bluebird", "blush", "board", "boast", "boat", "bob", "bobwhite", "bodies", "body", "boil", "boiler", "bold", "bone", "bonnet", "boo", "book", "bookcase", "bookkeeper", "boom", "boot", "born", "borrow", "boss", "both", "bother", "bottle", "bottom", "bought", "bounce", "bow", "bowl", "bow-wow", "box", "boxcar", "boxer", "boxes", "boy", "boyhood", "bracelet", "brain", "brake", "bran", "branch", "brass", "brave", "bread", "break", "breakfast", "breast", "breath", "breathe", "breeze", "brick", "bride", "bridge", "bright", "brightness", "bring", "broad", "broadcast", "broke", "broken", "brook", "broom", "brother", "brought", "brown", "brush", "bubble", "bucket", "buckle", "bud", "buffalo", "bug", "buggy", "build", "building", "built", "bulb", "bull", "bullet", "bum", "bumblebee", "bump", "bun", "bunch", "bundle", "bunny", "burn", "burst", "bury", "bus", "bush", "bushel", "business", "busy", "but", "butcher", "butt", "butter", "buttercup", "butterfly", "buttermilk", "butterscotch", "button", "buttonhole", "buy", "buzz", "by", "bye", "cab", "cabbage", "cabin", "cabinet", "cackle", "cage", "cake", "calendar", "calf", "call", "caller", "calling", "came", "camel", "camp", "campfire", "can", "canal", "canary", "candle", "candlestick", "candy", "cane", "cannon", "cannot", "canoe", "can\'t", "canyon", "cap", "cape", "capital", "captain", "car", "card", "cardboard", "care", "careful", "careless", "carelessness", "carload", "carpenter", "carpet", "carriage", "carrot", "carry", "cart", "carve", "case", "cash", "cashier", "castle", "cat", "catbird", "catch", "catcher", "caterpillar", "catfish", "catsup", "cattle", "caught", "cause", "cave", "ceiling", "cell", "cellar", "cent", "center", "cereal", "certain", "certainly", "chain", "chair", "chalk", "champion", "chance", "change", "chap", "charge", "charm", "chart", "chase", "chatter", "cheap", "cheat", "check", "checkers", "cheek", "cheer", "cheese", "cherry", "chest", "chew", "chick", "chicken", "chief", "child", "childhood", "children", "chill", "chilly", "chimney", "chin", "china", "chip", "chipmunk", "chocolate", "choice", "choose", "chop", "chorus", "chose", "chosen", "christen", "Christmas", "church", "churn", "cigarette", "circle", "circus", "citizen", "city", "clang", "clap", "class", "classmate", "classroom", "claw", "clay", "clean", "cleaner", "clear", "clerk", "clever", "click", "cliff", "climb", "clip", "cloak", "clock", "close", "closet", "cloth", "clothes", "clothing", "cloud", "cloudy", "clover", "clown", "club", "cluck", "clump", "coach", "coal", "coast", "coat", "cob", "cobbler", "cocoa", "coconut", "cocoon", "cod", "codfish", "coffee", "coffeepot", "coin", "cold", "collar", "college", "color", "colored", "colt", "column", "comb", "come", "comfort", "comic", "coming", "company", "compare", "conductor", "cone", "connect", "coo", "cook", "cooked", "cooking", "cookie", "cookies", "cool", "cooler", "coop", "copper", "copy", "cord", "cork", "corn", "corner", "correct", "cost", "cot", "cottage", "cotton", "couch", "cough", "could", "couldn\'t", "count", "counter", "country", "county", "course", "court", "cousin", "cover", "cow", "coward", "cowardly", "cowboy", "cozy", "crab", "crack", "cracker", "cradle", "cramps", "cranberry", "crank", "cranky", "crash", "crawl", "crazy", "cream", "creamy", "creek", "creep", "crept", "cried", "croak", "crook", "crooked", "crop", "cross", "crossing", "cross-eyed", "crow", "crowd", "crowded", "crown", "cruel", "crumb", "crumble", "crush", "crust", "cry", "cries", "cub", "cuff", "cup", "cuff", "cup", "cupboard", "cupful", "cure", "curl", "curly", "curtain", "curve", "cushion", "custard", "customer", "cut", "cute", "cutting", "dab", "dad", "daddy", "daily", "dairy", "daisy", "dam", "damage", "dame", "damp", "dance", "dancer", "dancing", "dandy", "danger", "dangerous", "dare", "dark", "darkness", "darling", "darn", "dart", "dash", "date", "daughter", "dawn", "day", "daybreak", "daytime", "dead", "deaf", "deal", "dear", "death", "December", "decide", "deck", "deed", "deep", "deer", "defeat", "defend", "defense", "delight", "den", "dentist", "depend", "deposit", "describe", "desert", "deserve", "desire", "desk", "destroy", "devil", "dew", "diamond", "did", "didn\'t", "die", "died", "dies", "difference", "different", "dig", "dim", "dime", "dine", "ding-dong", "dinner", "dip", "direct", "direction", "dirt", "dirty", "discover", "dish", "dislike", "dismiss", "ditch", "dive", "diver", "divide", "do", "dock", "doctor", "does", "doesn\'t", "dog", "doll", "dollar", "dolly", "done", "donkey", "don\'t", "door", "doorbell", "doorknob", "doorstep", "dope", "dot", "double", "dough", "dove", "down", "downstairs", "downtown", "dozen", "drag", "drain", "drank", "draw", "drawer", "draw", "drawing", "dream", "dress", "dresser", "dressmaker", "drew", "dried", "drift", "drill", "drink", "drip", "drive", "driven", "driver", "drop", "drove", "drown", "drowsy", "drub", "drum", "drunk", "dry", "duck", "due", "dug", "dull", "dumb", "dump", "during", "dust", "dusty", "duty", "dwarf", "dwell", "dwelt", "dying", "each", "eager", "eagle", "ear", "early", "earn", "earth", "east", "eastern", "easy", "eat", "eaten", "edge", "egg", "eh", "eight", "eighteen", "eighth", "eighty", "either", "elbow", "elder", "eldest", "electric", "electricity", "elephant", "eleven", "elf", "elm", "else", "elsewhere", "empty", "end", "ending", "enemy", "engine", "engineer", "English", "enjoy", "enough", "enter", "envelope", "equal", "erase", "eraser", "errand", "escape", "eve", "even", "evening", "ever", "every", "everybody", "everyday", "everyone", "everything", "everywhere", "evil", "exact", "except", "exchange", "excited", "exciting", "excuse", "exit", "expect", "explain", "extra", "eye", "eyebrow", "fable", "face", "facing", "fact", "factory", "fail", "faint", "fair", "fairy", "faith", "fake", "fall", "false", "family", "fan", "fancy", "far", "faraway", "fare", "farmer", "farm", "farming", "far-off", "farther", "fashion", "fast", "fasten", "fat", "father", "fault", "favor", "favorite", "fear", "feast", "feather", "February", "fed", "feed", "feel", "feet", "fell", "fellow", "felt", "fence", "fever", "few", "fib", "fiddle", "field", "fife", "fifteen", "fifth", "fifty", "fig", "fight", "figure", "file", "fill", "film", "finally", "find", "fine", "finger", "finish", "fire", "firearm", "firecracker", "fireplace", "fireworks", "firing", "first", "fish", "fisherman", "fist", "fit", "fits", "five", "fix", "flag", "flake", "flame", "flap", "flash", "flashlight", "flat", "flea", "flesh", "flew", "flies", "flight", "flip", "flip-flop", "float", "flock", "flood", "floor", "flop", "flour", "flow", "flower", "flowery", "flutter", "fly", "foam", "fog", "foggy", "fold", "folks", "follow", "following", "fond", "food", "fool", "foolish", "foot", "football", "footprint", "for", "forehead", "forest", "forget", "forgive", "forgot", "forgotten", "fork", "form", "fort", "forth", "fortune", "forty", "forward", "fought", "found", "fountain", "four", "fourteen", "fourth", "fox", "frame", "free", "freedom", "freeze", "freight", "French", "fresh", "fret", "Friday", "fried", "friend", "friendly", "friendship", "frighten", "frog", "from", "front", "frost", "frown", "froze", "fruit", "fry", "fudge", "fuel", "full", "fully", "fun", "funny", "fur", "furniture", "further", "fuzzy", "gain", "gallon", "gallop", "game", "gang", "garage", "garbage", "garden", "gas", "gasoline", "gate", "gather", "gave", "gay", "gear", "geese", "general", "gentle", "gentleman", "gentlemen", "geography", "get", "getting", "giant", "gift", "gingerbread", "girl", "give", "given", "giving", "glad", "gladly", "glance", "glass", "glasses", "gleam", "glide", "glory", "glove", "glow", "glue", "go", "going", "goes", "goal", "goat", "gobble", "God", "god", "godmother", "gold", "golden", "goldfish", "golf", "gone", "good", "goods", "goodbye", "good-by", "goodbye", "good-bye", "good-looking", "goodness", "goody", "goose", "gooseberry", "got", "govern", "government", "gown", "grab", "gracious", "grade", "grain", "grand", "grandchild", "grandchildren", "granddaughter", "grandfather", "grandma", "grandmother", "grandpa", "grandson", "grandstand", "grape", "grapes", "grapefruit", "grass", "grasshopper", "grateful", "grave", "gravel", "graveyard", "gravy", "gray", "graze", "grease", "great", "green", "greet", "grew", "grind", "groan", "grocery", "ground", "group", "grove", "grow", "guard", "guess", "guest", "guide", "gulf", "gum", "gun", "gunpowder", "guy", "ha", "habit", "had", "hadn\'t", "hail", "hair", "haircut", "hairpin", "half", "hall", "halt", "ham", "hammer", "hand", "handful", "handkerchief", "handle", "handwriting", "hang", "happen", "happily", "happiness", "happy", "harbor", "hard", "hardly", "hardship", "hardware", "hare", "hark", "harm", "harness", "harp", "harvest", "has", "hasn\'t", "haste", "hasten", "hasty", "hat", "hatch", "hatchet", "hate", "haul", "have", "haven\'t", "having", "hawk", "hay", "hayfield", "haystack", "he", "head", "headache", "heal", "health", "healthy", "heap", "hear", "hearing", "heard", "heart", "heat", "heater", "heaven", "heavy", "he\'d", "heel", "height", "held", "hell", "he\'ll", "hello", "helmet", "help", "helper", "helpful", "hem", "hen", "henhouse", "her", "hers", "herd", "here", "here\'s", "hero", "herself", "he\'s", "hey", "hickory", "hid", "hidden", "hide", "high", "highway", "hill", "hillside", "hilltop", "hilly", "him", "himself", "hind", "hint", "hip", "hire", "his", "hiss", "history", "hit", "hitch", "hive", "ho", "hoe", "hog", "hold", "holder", "hole", "holiday", "hollow", "holy", "home", "homely", "homesick", "honest", "honey", "honeybee", "honeymoon", "honk", "honor", "hood", "hoof", "hook", "hoop", "hop", "hope", "hopeful", "hopeless", "horn", "horse", "horseback", "horseshoe", "hose", "hospital", "host", "hot", "hotel", "hound", "hour", "house", "housetop", "housewife", "housework", "how", "however", "howl", "hug", "huge", "hum", "humble", "hump", "hundred", "hung", "hunger", "hungry", "hunk", "hunt", "hunter", "hurrah", "hurried", "hurry", "hurt", "husband", "hush", "hut", "hymn", "I", "ice", "icy", "I\'d", "idea", "ideal", "if", "ill", "I\'ll", "I\'m", "important", "impossible", "improve", "in", "inch", "inches", "income", "indeed", "Indian", "indoors", "ink", "inn", "insect", "inside", "instant", "instead", "insult", "intend", "interested", "interesting", "into", "invite", "iron", "is", "island", "isn\'t", "it", "its", "it\'s", "itself", "I\'ve", "ivory", "ivy", "jacket", "jacks", "jail", "jam", "January", "jar", "jaw", "jay", "jelly", "jellyfish", "jerk", "jig", "job", "jockey", "join", "joke", "joking", "jolly", "journey", "joy", "joyful", "joyous", "judge", "jug", "juice", "juicy", "July", "jump", "June", "junior", "junk", "just", "keen", "keep", "kept", "kettle", "key", "kick", "kid", "kill", "killed", "kind", "kindly", "kindness", "king", "kingdom", "kiss", "kitchen", "kite", "kitten", "kitty", "knee", "kneel", "knew", "knife", "knit", "knives", "knob", "knock", "knot", "know", "known", "lace", "lad", "ladder", "ladies", "lady", "laid", "lake", "lamb", "lame", "lamp", "land", "lane", "language", "lantern", "lap", "lard", "large", "lash", "lass", "last", "late", "laugh", "laundry", "law", "lawn", "lawyer", "lay", "lazy", "lead", "leader", "leaf", "leak", "lean", "leap", "learn", "learned", "least", "leather", "leave", "leaving", "led", "left", "leg", "lemon", "lemonade", "lend", "length", "less", "lesson", "let", "let\'s", "letter", "letting", "lettuce", "level", "liberty", "library", "lice", "lick", "lid", "lie", "life", "lift", "light", "lightness", "lightning", "like", "likely", "liking", "lily", "limb", "lime", "limp", "line", "linen", "lion", "lip", "list", "listen", "lit", "little", "live", "lives", "lively", "liver", "living", "lizard", "load", "loaf", "loan", "loaves", "lock", "locomotive", "log", "lone", "lonely", "lonesome", "long", "look", "lookout", "loop", "loose", "lord", "lose", "loser", "loss", "lost", "lot", "loud", "love", "lovely", "lover", "low", "luck", "lucky", "lumber", "lump", "lunch", "lying", "ma", "machine", "machinery", "mad", "made", "magazine", "magic", "maid", "mail", "mailbox", "mailman", "major", "make", "making", "male", "mama", "mamma", "man", "manager", "mane", "manger", "many", "map", "maple", "marble", "march", "March", "mare", "mark", "market", "marriage", "married", "marry", "mask", "mast", "master", "mat", "match", "matter", "mattress", "may", "May", "maybe", "mayor", "maypole", "me", "meadow", "meal", "mean", "means", "meant", "measure", "meat", "medicine", "meet", "meeting", "melt", "member", "men", "mend", "meow", "merry", "mess", "message", "met", "metal", "mew", "mice", "middle", "midnight", "might", "mighty", "mile", "milk", "milkman", "mill", "miler", "million", "mind", "mine", "miner", "mint", "minute", "mirror", "mischief", "miss", "Miss", "misspell", "mistake", "misty", "mitt", "mitten", "mix", "moment", "Monday", "money", "monkey", "month", "moo", "moon", "moonlight", "moose", "mop", "more", "morning", "morrow", "moss", "most", "mostly", "mother", "motor", "mount", "mountain", "mouse", "mouth", "move", "movie", "movies", "moving", "mow", "Mr.", "Mrs.", "much", "mud", "muddy", "mug", "mule", "multiply", "murder", "music", "must", "my", "myself", "nail", "name", "nap", "napkin", "narrow", "nasty", "naughty", "navy", "near", "nearby", "nearly", "neat", "neck", "necktie", "need", "needle", "needn\'t", "Negro", "neighbor", "neighborhood", "neither", "nerve", "nest", "net", "never", "nevermore", "new", "news", "newspaper", "next", "nibble", "nice", "nickel", "night", "nightgown", "nine", "nineteen", "ninety", "no", "nobody", "nod", "noise", "noisy", "none", "noon", "nor", "north", "northern", "nose", "not", "note", "nothing", "notice", "November", "now", "nowhere", "number", "nurse", "nut", "oak", "oar", "oatmeal", "oats", "obey", "ocean", "o\'clock", "October", "odd", "of", "off", "offer", "office", "officer", "often", "oh", "oil", "old", "old-fashioned", "on", "once", "one", "onion", "only", "onward", "open", "or", "orange", "orchard", "order", "ore", "organ", "other", "otherwise", "ouch", "ought", "our", "ours", "ourselves", "out", "outdoors", "outfit", "outlaw", "outline", "outside", "outward", "oven", "over", "overalls", "overcoat", "overeat", "overhead", "overhear", "overnight", "overturn", "owe", "owing", "owl", "own", "owner", "ox", "pa", "pace", "pack", "package", "pad", "page", "paid", "pail", "pain", "painful", "paint", "painter", "painting", "pair", "pal", "palace", "pale", "pan", "pancake", "pane", "pansy", "pants", "papa", "paper", "parade", "pardon", "parent", "park", "part", "partly", "partner", "party", "pass", "passenger", "past", "paste", "pasture", "pat", "patch", "path", "patter", "pave", "pavement", "paw", "pay", "payment", "pea", "peas", "peace", "peaceful", "peach", "peaches", "peak", "peanut", "pear", "pearl", "peck", "peek", "peel", "peep", "peg", "pen", "pencil", "penny", "people", "pepper", "peppermint", "perfume", "perhaps", "person", "pet", "phone", "piano", "pick", "pickle", "picnic", "picture", "pie", "piece", "pig", "pigeon", "piggy", "pile", "pill", "pillow", "pin", "pine", "pineapple", "pink", "pint", "pipe", "pistol", "pit", "pitch", "pitcher", "pity", "place", "plain", "plan", "plane", "plant", "plate", "platform", "platter", "play", "player", "playground", "playhouse", "playmate", "plaything", "pleasant", "please", "pleasure", "plenty", "plow", "plug", "plum", "pocket", "pocketbook", "poem", "point", "poison", "poke", "pole", "police", "policeman", "polish", "polite", "pond", "ponies", "pony", "pool", "poor", "pop", "popcorn", "popped", "porch", "pork", "possible", "post", "postage", "postman", "pot", "potato", "potatoes", "pound", "pour", "powder", "power", "powerful", "praise", "pray", "prayer", "prepare", "present", "pretty", "price", "prick", "prince", "princess", "print", "prison", "prize", "promise", "proper", "protect", "proud", "prove", "prune", "public", "puddle", "puff", "pull", "pump", "pumpkin", "punch", "punish", "pup", "pupil", "puppy", "pure", "purple", "purse", "push", "puss", "pussy", "pussycat", "put", "putting", "puzzle", "quack", "quart", "quarter", "queen", "queer", "question", "quick", "quickly", "quiet", "quilt", "quit", "quite", "rabbit", "race", "rack", "radio", "radish", "rag", "rail", "railroad", "railway", "rain", "rainy", "rainbow", "raise", "raisin", "rake", "ram", "ran", "ranch", "rang", "rap", "rapidly", "rat", "rate", "rather", "rattle", "raw", "ray", "reach", "read", "reader", "reading", "ready", "real", "really", "reap", "rear", "reason", "rebuild", "receive", "recess", "record", "red", "redbird", "redbreast", "refuse", "reindeer", "rejoice", "remain", "remember", "remind", "remove", "rent", "repair", "repay", "repeat", "report", "rest", "return", "review", "reward", "rib", "ribbon", "rice", "rich", "rid", "riddle", "ride", "rider", "riding", "right", "rim", "ring", "rip", "ripe", "rise", "rising", "river", "road", "roadside", "roar", "roast", "rob", "robber", "robe", "robin", "rock", "rocky", "rocket", "rode", "roll", "roller", "roof", "room", "rooster", "root", "rope", "rose", "rosebud", "rot", "rotten", "rough", "round", "route", "row", "rowboat", "royal", "rub", "rubbed", "rubber", "rubbish", "rug", "rule", "ruler", "rumble", "run", "rung", "runner", "running", "rush", "rust", "rusty", "rye", "sack", "sad", "saddle", "sadness", "safe", "safety", "said", "sail", "sailboat", "sailor", "saint", "salad", "sale", "salt", "same", "sand", "sandy", "sandwich", "sang", "sank", "sap", "sash", "sat", "satin", "satisfactory", "Saturday", "sausage", "savage", "save", "savings", "saw", "say", "scab", "scales", "scare", "scarf", "school", "schoolboy", "schoolhouse", "schoolmaster", "schoolroom", "scorch", "score", "scrap", "scrape", "scratch", "scream", "screen", "screw", "scrub", "sea", "seal", "seam", "search", "season", "seat", "second", "secret", "see", "seeing", "seed", "seek", "seem", "seen", "seesaw", "select", "self", "selfish", "sell", "send", "sense", "sent", "sentence", "separate", "September", "servant", "serve", "service", "set", "setting", "settle", "settlement", "seven", "seventeen", "seventh", "seventy", "several", "sew", "shade", "shadow", "shady", "shake", "shaker", "shaking", "shall", "shame", "shan\'t", "shape", "share", "sharp", "shave", "she", "she\'d", "she\'ll", "she\'s", "shear", "shears", "shed", "sheep", "sheet", "shelf", "shell", "shepherd", "shine", "shining", "shiny", "ship", "shirt", "shock", "shoe", "shoemaker", "shone", "shook", "shoot", "shop", "shopping", "shore", "short", "shot", "should", "shoulder", "shouldn\'t", "shout", "shovel", "show", "shower", "shut", "shy", "sick", "sickness", "side", "sidewalk", "sideways", "sigh", "sight", "sign", "silence", "silent", "silk", "sill", "silly", "silver", "simple", "sin", "since", "sing", "singer", "single", "sink", "sip", "sir", "sis", "sissy", "sister", "sit", "sitting", "six", "sixteen", "sixth", "sixty", "size", "skate", "skater", "ski", "skin", "skip", "skirt", "sky", "slam", "slap", "slate", "slave", "sled", "sleep", "sleepy", "sleeve", "sleigh", "slept", "slice", "slid", "slide", "sling", "slip", "slipped", "slipper", "slippery", "slit", "slow", "slowly", "sly", "smack", "small", "smart", "smell", "smile", "smoke", "smooth", "snail", "snake", "snap", "snapping", "sneeze", "snow", "snowy", "snowball", "snowflake", "snuff", "snug", "so", "soak", "soap", "sob", "socks", "sod", "soda", "sofa", "soft", "soil", "sold", "soldier", "sole", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "son", "song", "soon", "sore", "sorrow", "sorry", "sort", "soul", "sound", "soup", "sour", "south", "southern", "space", "spade", "spank", "sparrow", "speak", "speaker", "spear", "speech", "speed", "spell", "spelling", "spend", "spent", "spider", "spike", "spill", "spin", "spinach", "spirit", "spit", "splash", "spoil", "spoke", "spook", "spoon", "sport", "spot", "spread", "spring", "springtime", "sprinkle", "square", "squash", "squeak", "squeeze", "squirrel", "stable", "stack", "stage", "stair", "stall", "stamp", "stand", "star", "stare", "start", "starve", "state", "station", "stay", "steak", "steal", "steam", "steamboat", "steamer", "steel", "steep", "steeple", "steer", "stem", "step", "stepping", "stick", "sticky", "stiff", "still", "stillness", "sting", "stir", "stitch", "stock", "stocking", "stole", "stone", "stood", "stool", "stoop", "stop", "stopped", "stopping", "store", "stork", "stories", "storm", "stormy", "story", "stove", "straight", "strange", "stranger", "strap", "straw", "strawberry", "stream", "street", "stretch", "string", "strip", "stripes", "strong", "stuck", "study", "stuff", "stump", "stung", "subject", "such", "suck", "sudden", "suffer", "sugar", "suit", "sum", "summer", "sun", "Sunday", "sunflower", "sung", "sunk", "sunlight", "sunny", "sunrise", "sunset", "sunshine", "supper", "suppose", "sure", "surely", "surface", "surprise", "swallow", "swam", "swamp", "swan", "swat", "swear", "sweat", "sweater", "sweep", "sweet", "sweetness", "sweetheart", "swell", "swept", "swift", "swim", "swimming", "swing", "switch", "sword", "swore", "table", "tablecloth", "tablespoon", "tablet", "tack", "tag", "tail", "tailor", "take", "taken", "taking", "tale", "talk", "talker", "tall", "tame", "tan", "tank", "tap", "tape", "tar", "tardy", "task", "taste", "taught", "tax", "tea", "teach", "teacher", "team", "tear", "tease", "teaspoon", "teeth", "telephone", "tell", "temper", "ten", "tennis", "tent", "term", "terrible", "test", "than", "thank", "thanks", "thankful", "Thanksgiving", "that", "that\'s", "the", "theater", "thee", "their", "them", "then", "there", "these", "they", "they\'d", "they\'ll", "they\'re", "they\'ve", "thick", "thief", "thimble", "thin", "thing", "think", "third", "thirsty", "thirteen", "thirty", "this", "thorn", "those", "though", "thought", "thousand", "thread", "three", "threw", "throat", "throne", "through", "throw", "thrown", "thumb", "thunder", "Thursday", "thy", "tick", "ticket", "tickle", "tie", "tiger", "tight", "till", "time", "tin", "tinkle", "tiny", "tip", "tiptoe", "tire", "tired", "title", "to", "toad", "toadstool", "toast", "tobacco", "today", "toe", "together", "toilet", "told", "tomato", "tomorrow", "ton", "tone", "tongue", "tonight", "too", "took", "tool", "toot", "tooth", "toothbrush", "toothpick", "top", "tore", "torn", "toss", "touch", "tow", "toward", "towards", "towel", "tower", "town", "toy", "trace", "track", "trade", "train", "tramp", "trap", "tray", "treasure", "treat", "tree", "trick", "tricycle", "tried", "trim", "trip", "trolley", "trouble", "truck", "true", "truly", "trunk", "trust", "truth", "try", "tub", "Tuesday", "tug", "tulip", "tumble", "tune", "tunnel", "turkey", "turn", "turtle", "twelve", "twenty", "twice", "twig", "twin", "two", "ugly", "umbrella", "uncle", "under", "understand", "underwear", "undress", "unfair", "unfinished", "unfold", "unfriendly", "unhappy", "unhurt", "uniform", "United", "States", "unkind", "unknown", "unless", "unpleasant", "until", "unwilling", "up", "upon", "upper", "upset", "upside", "upstairs", "uptown", "upward", "us", "use", "used", "useful", "valentine", "valley", "valuable", "value", "vase", "vegetable", "velvet", "very", "vessel", "victory", "view", "village", "vine", "violet", "visit", "visitor", "voice", "vote", "wag", "wagon", "waist", "wait", "wake", "waken", "walk", "wall", "walnut", "want", "war", "warm", "warn", "was", "wash", "washer", "washtub", "wasn\'t", "waste", "watch", "watchman", "water", "watermelon", "waterproof", "wave", "wax", "way", "wayside", "we", "weak", "weakness", "weaken", "wealth", "weapon", "wear", "weary", "weather", "weave", "web", "we\'d", "wedding", "Wednesday", "wee", "weed", "week", "we\'ll", "weep", "weigh", "welcome", "well", "went", "were", "we\'re", "west", "western", "wet", "we\'ve", "whale", "what", "what\'s", "wheat", "wheel", "when", "whenever", "where", "which", "while", "whip", "whipped", "whirl", "whisky", "whiskey", "whisper", "whistle", "white", "who", "who\'d", "whole", "who\'ll", "whom", "who\'s", "whose", "why", "wicked", "wide", "wife", "wiggle", "wild", "wildcat", "will", "willing", "willow", "win", "wind", "windy", "windmill", "window", "wine", "wing", "wink", "winner", "winter", "wipe", "wire", "wise", "wish", "wit", "witch", "with", "without", "woke", "wolf", "woman", "women", "won", "wonder", "wonderful", "won\'t", "wood", "wooden", "woodpecker", "woods", "wool", "woolen", "word", "wore", "work", "worker", "workman", "world", "worm", "worn", "worry", "worse", "worst", "worth", "would", "wouldn\'t", "wound", "wove", "wrap", "wrapped", "wreck", "wren", "wring", "write", "writing", "written", "wrong", "wrote", "wrung", "yard", "yarn", "year", "yell", "yellow", "yes", "yesterday", "yet", "yolk", "yonder", "you", "you\'d", "you\'ll", "young", "youngster", "your", "yours", "you\'re", "yourself", "yourselves"));
    protected static Set<String> arrSpacheWordList = new HashSet<>(Arrays.asList("a", "able", "about", "above", "across", "act", "add", "afraid", "after", "afternoon", "again", "against", "ago", "air", "airplane", "alarm", "all", "almost", "alone", "along", "already", "also", "always", "am", "among", "an", "and", "angry", "animal", "another", "answer", "any", "anyone", "appear", "apple", "are", "arm", "around", "arrow", "as", "ask", "asleep", "at", "ate", "attention", "aunt", "awake", "away", "b", "baby", "back", "bad", "bag", "ball", "balloon", "bang", "bank", "bark", "barn", "basket", "be", "bean", "bear", "beat", "beautiful", "became", "because", "become", "bed", "bee", "been", "before", "began", "begin", "behind", "believe", "bell", "belong", "bend", "bent", "beside", "best", "better", "between", "big", "bird", "birthday", "bit", "bite", "black", "blanket", "blew", "block", "blow", "blue", "board", "boat", "book", "boot", "born", "borrow", "both", "bother", "bottle", "bottom", "bought", "bow", "box", "boy", "branch", "brave", "bread", "break", "breakfast", "breath", "brick", "bridge", "bright", "bring", "broke", "broken", "brother", "brought", "brown", "brush", "build", "bump", "burn", "bus", "busy", "but", "butter", "button", "buy", "by", "c", "cabin", "cage", "cake", "call", "came", "camp", "can", "candle", "candy", "can\'t", "cap", "captain", "car", "card", "care", "careful", "carrot", "carry", "case", "castle", "cat", "catch", "cattle", "caught", "cause", "cent", "certain", "chair", "chance", "change", "chase", "chicken", "chief", "child", "children", "church", "circle", "circus", "city", "clap", "clean", "clever", "cliff", "climb", "clock", "close", "cloth", "clothes", "clown", "coat", "cold", "color", "come", "comfortable", "company", "contest", "continue", "cook", "cool", "corner", "could", "count", "country", "course", "cover", "cow", "crawl", "cream", "cry", "cup", "curtain", "cut", "d", "Dad", "dance", "danger", "dangerous", "dark", "dash", "daughter", "day", "dear", "decide", "deep", "desk", "did", "didn\'t", "die", "different", "dig", "dinner", "direction", "disappear", "disappoint", "discover", "distance", "do", "doctor", "does", "dog", "dollar", "done", "don\'t", "door", "down", "dragon", "dream", "dress", "drink", "drive", "drop", "drove", "dry", "duck", "during", "dust", "e", "each", "eager", "ear", "early", "earn", "earth", "easy", "eat", "edge", "egg", "eight", "eighteen", "either", "elephant", "else", "empty", "end", "enemy", "enough", "enter", "even", "ever", "every", "everything", "exact", "except", "excite", "exclaim", "explain", "eye", "face", "fact", "fair", "fall", "family", "far", "farm", "farmer", "farther", "fast", "fat", "father", "feather", "feed", "feel", "feet", "fell", "fellow", "felt", "fence", "few", "field", "fierce", "fight", "figure", "fill", "final", "find", "fine", "finger", "finish", "fire", "first", "fish", "five", "flag", "flash", "flat", "flew", "floor", "flower", "fly", "follow", "food", "for", "forest", "forget", "forth", "found", "four", "fourth", "fox", "fresh", "friend", "frighten", "frog", "from", "front", "fruit", "full", "fun", "funny", "fur", "g", "game", "garden", "gasp", "gate", "gave", "get", "giant", "gift", "girl", "give", "glad", "glass", "go", "goat", "gone", "good", "got", "grandfather", "grandmother", "grass", "gray", "great", "green", "grew", "grin", "ground", "group", "grow", "growl", "guess", "gun", "h", "had", "hair", "half", "hall", "hand", "handle", "hang", "happen", "happiness", "happy", "hard", "harm", "has", "hat", "hate", "have", "he", "head", "hear", "heard", "heavy", "held", "hello", "help", "hen", "her", "here", "herself", "he\'s", "hid", "hide", "high", "hill", "him", "himself", "his", "hit", "hold", "hole", "holiday", "home", "honey", "hop", "horn", "horse", "hot", "hour", "house", "how", "howl", "hum", "hundred", "hung", "hungry", "hunt", "hurry", "hurt", "husband", "i", "I", "ice", "idea", "if", "I\'ll", "I\'m", "imagine", "important", "in", "inch", "indeed", "inside", "instead", "into", "invite", "is", "it", "it\'s", "its", "j", "jacket", "jar", "jet", "job", "join", "joke", "joy", "jump", "just", "k", "keep", "kept", "key", "kick", "kill", "kind", "king", "kitchen", "kitten", "knee", "knew", "knock", "know", "l", "ladder", "lady", "laid", "lake", "land", "large", "last", "late", "laugh", "lay", "lazy", "lead", "leap", "learn", "least", "leave", "left", "leg", "less", "let", "let\'s", "letter", "lick", "lift", "light", "like", "line", "lion", "list", "listen", "little", "live", "load", "long", "look", "lost", "lot", "loud", "love", "low", "luck", "lump", "lunch", "m", "machine", "made", "magic", "mail", "make", "man", "many", "march", "mark", "market", "master", "matter", "may", "maybe", "me", "mean", "meant", "meat", "meet", "melt", "men", "merry", "met", "middle", "might", "mile", "milk", "milkman", "mind", "mine", "minute", "miss", "mistake", "moment", "money", "monkey", "month", "more", "morning", "most", "mother", "mountain", "mouse", "mouth", "move", "much", "mud", "music", "must", "my", "n", "name", "near", "neck", "need", "needle", "neighbor", "neighborhood", "nest", "never", "new", "next", "nibble", "nice", "night", "nine", "no", "nod", "noise", "none", "north", "nose", "not", "note", "nothing", "notice", "now", "number", "o", "ocean", "of", "off", "offer", "often", "oh", "old", "on", "once", "one", "only", "open", "or", "orange", "order", "other", "our", "out", "outside", "over", "owl", "own", "p", "pack", "paid", "pail", "paint", "pair", "palace", "pan", "paper", "parade", "parent", "park", "part", "party", "pass", "past", "pasture", "path", "paw", "pay", "peanut", "peek", "pen", "penny", "people", "perfect", "perhaps", "person", "pet", "pick", "picnic", "picture", "pie", "piece", "pig", "pile", "pin", "place", "plan", "plant", "play", "pleasant", "please", "plenty", "plow", "picket", "point", "poke", "pole", "policeman", "pond", "poor", "pop", "postman", "pot", "potato", "pound", "pour", "practice", "prepare", "present", "pretend", "pretty", "princess", "prize", "probably", "problem", "promise", "protect", "proud", "puff", "pull", "puppy", "push", "put", "q", "queen", "queer", "quick", "quiet", "quite", "r", "rabbit", "raccoon", "race", "radio", "rag", "rain", "raise", "ran", "ranch", "rang", "reach", "read", "ready", "real", "red", "refuse", "remember", "reply", "rest", "return", "reward", "rich", "ride", "right", "ring", "river", "road", "roar", "rock", "rode", "roll", "roof", "room", "rope", "round", "row", "rub", "rule", "run", "rush", "s", "sad", "safe", "said", "sail", "sale", "salt", "same", "sand", "sang", "sat", "save", "saw", "say", "scare", "school", "scold", "scratch", "scream", "sea", "seat", "second", "secret", "see", "seed", "seem", "seen", "sell", "send", "sent", "seven", "several", "sew", "shadow", "shake", "shall", "shape", "she", "sheep", "shell", "shine", "ship", "shoe", "shone", "shook", "shoot", "shop", "shore", "short", "shot", "should", "show", "sick", "side", "sight", "sign", "signal", "silent", "silly", "silver", "since", "sing", "sister", "sit", "six", "size", "skip", "sky", "sled", "sleep", "slid", "slide", "slow", "small", "smart", "smell", "smile", "smoke", "snap", "sniff", "snow", "so", "soft", "sold", "some", "something", "sometimes", "son", "song", "soon", "sorry", "sound", "speak", "special", "spend", "spill", "splash", "spoke", "spot", "spread", "spring", "squirrel", "stand", "star", "start", "station", "stay", "step", "stick", "still", "stone", "stood", "stop", "store", "story", "straight", "strange", "street", "stretch", "strike", "strong", "such", "sudden", "sugar", "suit", "summer", "sun", "supper", "suppose", "sure", "surprise", "swallow", "sweet", "swim", "swing", "t", "table", "tail", "take", "talk", "tall", "tap", "taste", "teach", "teacher", "team", "tear", "teeth", "telephone", "tell", "ten", "tent", "than", "thank", "that", "that\'s", "the", "their", "them", "then", "there", "these", "they", "thick", "thin", "thing", "think", "third", "this", "those", "though", "thought", "three", "threw", "through", "throw", "tie", "tiger", "tight", "time", "tiny", "tip", "tire", "to", "today", "toe", "together", "told", "tomorrow", "too", "took", "tooth", "top", "touch", "toward", "tower", "town", "toy", "track", "traffic", "train", "trap", "tree", "trick", "trip", "trot", "truck", "true", "trunk", "try", "turkey", "turn", "turtle", "twelve", "twin", "two", "u", "ugly", "uncle", "under", "unhappy", "until", "up", "upon", "upstairs", "us", "use", "usual", "v", "valley", "vegetable", "very", "village", "visit", "voice", "w", "wag", "wagon", "wait", "wake", "walk", "want", "war", "warm", "was", "wash", "waste", "watch", "water", "wave", "way", "we", "wear", "weather", "week", "well", "went", "were", "wet", "what", "wheel", "when", "where", "which", "while", "whisper", "whistle", "white", "who", "whole", "whose", "why", "wide", "wife", "will", "win", "wind", "window", "wing", "wink", "winter", "wire", "wise", "wish", "with", "without", "woke", "wolf", "woman", "women", "wonder", "won\'t", "wood", "word", "wore", "work", "world", "worm", "worry", "worth", "would", "wrong", "x", "y", "yard", "year", "yell", "yellow", "yes", "yet", "you", "young", "your", "z", "zoo"));
    
    // These syllables would be counted as two but should be one
    protected static List<String> arrSubSyllables = Arrays.asList("cial", "tia", "cius", "cious", "giu", "ion", "iou", "sia$", "[^aeiuoyt]{2,}ed$", ".ely$", "[cg]h?e[rsd]?$", "rved?$", "[aeiouy][dt]es?$", "[aeiouy][^aeiouydt]e[rsd]?$", "[aeiouy]rse$");
        
    // These syllables would be counted as one but should be two
    protected static List<String> arrAddSyllables = Arrays.asList("ia", "riet", "dien", "iu", "io", "ii", "[aeiouym]bl$", "[aeiou]{3}", "^mc", "ism$", "([^aeiouy])\\1l$", "[^l]lien", "^coa[dglx].", "[^gq]ua[^auieo]", "dnt$", "uity$", "ie(r|st)$");

    // Single syllable prefixes and suffixes
    protected static List<String> arrPrefixSuffix = Arrays.asList("^un", "^fore", "ly$", "less$", "ful$", "ers?$", "ings?$");

    
    /**
     * Gives the Flesch-Kincaid Reading Ease of text entered rounded to one digit
     * @param   strText         Text to be checked
     * @return 
     */
    public static double flesch_kincaid_reading_ease(String strText) {
        strText = clean_text(strText);
        return PHPfunctions.round((206.835 - (1.015 * average_words_per_sentence(strText)) - (84.6 * average_syllables_per_word(strText))), 1);
    }

    /**
     * Gives the Flesch-Kincaid Grade level of text entered rounded to one digit
     * @param   strText         Text to be checked
     * @return 
     */
    public static double flesch_kincaid_grade_level(String strText) {
        strText = clean_text(strText);
        return PHPfunctions.round(((0.39 * average_words_per_sentence(strText)) + (11.8 * average_syllables_per_word(strText)) - 15.59), 1);
    }

    /**
     * Gives the Gunning-Fog score of text entered rounded to one digit
     * @param   strText         Text to be checked
     * @return 
     */
    public static double gunning_fog_score(String strText) {
        strText = clean_text(strText);
        return PHPfunctions.round(((average_words_per_sentence(strText) + percentage_words_with_three_syllables(strText)) * 0.4), 1);
    }

    /**
     * Gives the Coleman-Liau Index of text entered rounded to one digit
     * @param   strText         Text to be checked
     * @return 
     */
    public static double coleman_liau_index(String strText) {
        strText = clean_text(strText);
        int intWordCount = word_count(strText);
        return PHPfunctions.round( ( (5.89 * (letter_count(strText) / (double)intWordCount)) - (0.3 * (sentence_count(strText) / (double)intWordCount)) - 15.8 ), 1);
    }

    /**
     * Gives the SMOG Index of text entered rounded to one digit
     * @param   strText         Text to be checked
     * @return 
     */
    public static double smog_index(String strText) {
        strText = clean_text(strText);
        return PHPfunctions.round(1.043 * Math.sqrt((words_with_three_syllables(strText) * (30.0 / sentence_count(strText))) + 3.1291), 1);
    }

    /**
     * Gives the Automated Readability Index of text entered rounded to one digit
     * @param   strText         Text to be checked
     * @return 
     */
    public static double automated_readability_index(String strText) {
        strText = clean_text(strText);
        int intWordCount = word_count(strText);
        return PHPfunctions.round(((4.71 * (letter_count(strText) / (double)intWordCount)) + (0.5 * (intWordCount / (double)sentence_count(strText))) - 21.43), 1);
    }
    
    public static double dale_chall_score(String strText) {
        strText = clean_text(strText);
        int intDifficultWordCount = 0;
        List<String> arrWords = (new WhitespaceTokenizer()).tokenize(strText);
        int intWordCount = arrWords.size();
        for (int i = 0; i < intWordCount; ++i) {
            if (!arrDaleChallWordList.contains(arrWords.get(i))) {
                ++intDifficultWordCount;
            }
        }
        int intSentenceCount=sentence_count(strText);

        double percentageOfDifficultWords=intDifficultWordCount/(double)intWordCount;
        double score=0.1579*(100*percentageOfDifficultWords)+0.0496*(intWordCount/(double)intSentenceCount);
        if(percentageOfDifficultWords>0.05) {
            score+=3.6365;
        }

        return score;
    }

    public static double dale_chall_grade(String strText) {
        //http://rfptemplates.technologyevaluation.com/dale-chall-list-of-3000-simple-words.html
        double score=dale_chall_score(strText);
        if(score<5.0) {
            return 2.5;
        }
        else if(score<6.0) {
            return 5.5;
        }
        else if(score<7.0) {
            return 7.5;
        }
        else if(score<8.0) {
            return 9.5;
        }
        else if(score<9.0) {
            return 11.5;
        }
        else if(score<10.0) {
            return 14.0;
        }
        else {
            return 16.0;
        }
    }

    public static double spache_score(String strText) {
        //http://simple.wikipedia.org/wiki/Spache_Readability_Formula
        strText = clean_text(strText);
        int intUniqueUnfamiliarWordCount = 0;
        Set<String> arrWords = new HashSet<>((new WhitespaceTokenizer()).tokenize(strText));
        for(String word : arrWords) {
            if (!arrSpacheWordList.contains(word)) {
                ++intUniqueUnfamiliarWordCount;
            }
        }
        int intSentenceCount=sentence_count(strText);
        int intWordCount = word_count(strText);

        return 0.121*intWordCount/(double)intSentenceCount+0.082*intUniqueUnfamiliarWordCount+0.659;
    }
    
    /**
     * Returns word count for text.
     * @param   strText      Text to be measured
     * @return 
     */
    protected static int word_count(String strText) {
        return PHPfunctions.substr_count(strText, ' ')+1; // Space count + 1 is word count
    }

    /**
     * Gives letter count (ignores all non-letters). Tries mb_strlen and if that fails uses regular strlen.
     * @param   strText      Text to be measured
     * @return 
     */
    protected static int letter_count(String strText) {
        return strText.replaceAll("[^\\p{L}0-9]", "").length(); //remove all non-alphanumerics
    }

    /**
     * Returns sentence count for text.
     * @param   strText      Text to be measured
     * @return 
     */
    protected static int sentence_count(String strText) {
        int numberOfDots=PHPfunctions.substr_count(strText, '.');
        // Will be tripped up by "Mr." or "U.K.". Not a major concern at this point.
        if(strText.charAt(strText.length()-1)!='.') { //missing the final dot, count it too
            ++numberOfDots;
        }
        return Math.max(1, numberOfDots);
    }
    
    /**
     * Trims, removes line breaks, multiple spaces and generally cleans text before processing.
     * @param   strText      Text to be transformed
     * @return 
     */
    protected static String clean_text(String strText) {
        strText = HTMLCleaner.unsafeRemoveAllTags(strText);
        strText = strText.toLowerCase();
        
        strText = StringCleaner.unifyTerminators(strText);
        
        strText = strText.replaceAll(" [0-9]+ ", " "); // Remove "words" comprised only of numbers
        
        strText = StringCleaner.removeExtraSpaces(strText);
        return strText;
    }

    /**
     * Returns average words per sentence for text.
     * @param   strText      Text to be measured
     * @return 
     */
    protected static double average_words_per_sentence(String strText) {
        int intSentenceCount = sentence_count(strText);
        int intWordCount = word_count(strText);
        return (intWordCount / (double)intSentenceCount);
    }

    /**
     * Returns total syllable count for text.
     * @param   strText      Text to be measured
     * @return 
     */
    protected static int total_syllables(String strText) {
        int intSyllableCount = 0;
        
        List<String> arrWords = (new WhitespaceTokenizer()).tokenize(strText);
        int intWordCount = arrWords.size();
        for (int i = 0; i < intWordCount; ++i) {
            intSyllableCount += syllable_count(arrWords.get(i));
        }
        return intSyllableCount;
    }
    
    /**
     * Returns average syllables per word for text.
     * @param   strText      Text to be measured
     * @return 
     */
    protected static double average_syllables_per_word(String strText) {
        int intSyllableCount = total_syllables(strText);
        int intWordCount = word_count(strText);
        
        return (intSyllableCount / (double)intWordCount);
    }

    /**
     * Returns the number of words with more than three syllables
     * @param   strText                  Text to be measured
     * @return 
     */
    protected static int words_with_three_syllables(String strText) {
        int intLongWordCount = 0;
        
        List<String> arrWords = (new WhitespaceTokenizer()).tokenize(strText);
        int intWordCount = arrWords.size();
        for (int i = 0; i < intWordCount; ++i) {
            if(syllable_count(arrWords.get(i)) > 2) {
                ++intLongWordCount; //it also counts the proper nouns which should be excluded for Fog index, but this is not a major issue
            }
        }
        
        return intLongWordCount;
    }
    
    /**
     * Returns the percentage of words with more than three syllables
     * @param   strText      Text to be measured
     * @return 
     */
    protected static double percentage_words_with_three_syllables(String strText) {
        int intWordCount = word_count(strText);
        int intLongWordCount = words_with_three_syllables(strText);
        double percentage = ((intLongWordCount / (double)intWordCount) * 100.0);
        return percentage;
    }
    
    
    
    /**
     * Returns the number of syllables in the word.
     * Based in part on Greg Fast's Perl module Lingua::EN::Syllables
     * @param   strWord      Word to be measured
     * @return 
     */
    protected static int syllable_count(String strWord) {

        int intSyllableCount = 0;
        
        // Should be no non-alpha characters
        strWord = StringCleaner.removeSymbols(strWord).toLowerCase();


        // Specific common exceptions that don't follow the rule set below are handled individually
        // Array of problem words (with word as key, syllable count as value)
        Map<String, Integer> arrProblemWords = new HashMap<>();
        arrProblemWords.put("simile", 3);
        arrProblemWords.put("forever", 3);
        arrProblemWords.put("shoreline", 2);
        
        if (arrProblemWords.containsKey(strWord)) {
            return arrProblemWords.get(strWord);
        }


        // Remove prefixes and suffixes and count how many were taken
        int intPrefixSuffixCount =0;
        for(String regex : arrPrefixSuffix) {
            Pattern p = Pattern.compile(regex);

            Matcher m = p.matcher(strWord);
            StringBuffer sb = new StringBuffer(strWord.length());
            while(m.find()){ 
                m.appendReplacement(sb, ""); 
                ++intPrefixSuffixCount;
            }
            m.appendTail(sb);
            strWord = sb.toString();
        }
        // Removed non-word characters from word
        strWord = StringCleaner.removeSymbols(strWord);
        String[] arrWordParts = strWord.split("[^aeiouy]+");
        int intWordPartCount = 0;
        for(String strWordPart : arrWordParts) {
            if(!strWordPart.isEmpty()) {
                ++intWordPartCount;
            }
        }

        // Some syllables do not follow normal rules - check for them
        // Thanks to Joe Kovar for correcting a bug in the following lines
        intSyllableCount = intWordPartCount + intPrefixSuffixCount;
        for(String strSyllable : arrSubSyllables) {
            intSyllableCount -= PHPfunctions.preg_match(strSyllable, strWord);
        }
        for(String strSyllable : arrAddSyllables) {
            intSyllableCount += PHPfunctions.preg_match(strSyllable, strWord);
        }
        
        intSyllableCount = (intSyllableCount == 0) ? 1 : intSyllableCount;
        
        return intSyllableCount;

    }


}
