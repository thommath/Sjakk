import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Exception;

class Chess{
	public static void main(String[] args){
		Scanner in = new Scanner(System.in);
		
		Chessboard board = new Chessboard();
		board.analyze();	
		board.print();
		board.printDefenders();
		board.printAttackers();
		try{
			
		board.moveWhite();
		
		}catch(FinnerIkkeBrikkeException e){
			System.out.println(e);
		}
		board.print();
		
		while(true){
			try{
				String input = in.nextLine();
				String[] inp = input.split(" ");
				board.movePlayer(inp[0], inp[1]);
				
				board.analyze();
				board.printAttackers();
				board.moveWhite();
				board.print();
			}catch(FeilInputException e){
				System.out.println("Feil input");
			}catch(FinnerIkkeBrikkeException e){
				System.out.println(e);
			}
		}
	}
}

class Chessboard{
	Piece[][] board = new Piece[8][8];
	String[] bokstav = {"a", "b", "c", "d", "e", "f", "g", "h"};
	
	Chessboard(){
		boolean b = true;
		Piece[] white = {new Rook(b), new Knight(b), new Bishop(b), new Queen(b), new King(b), new Bishop(b), new Knight(b), new Rook(b)};
		b = false;
		Piece[] black = {new Rook(b), new Knight(b), new Bishop(b), new Queen(b), new King(b), new Bishop(b), new Knight(b), new Rook(b)};

		for(int n = 0; n < white.length; n++){
			board[n][0] = white[n];
			board[n][1] = new Pawn(true);
			board[n][6] = new Pawn(false);
			board[n][7] = black[n];
		}
	}
	
	Chessboard(Piece[][] bd){
		for(int n = 0; n < bd.length; n++){
			for(int m = 0; m < bd[n].length; m++){
				if(bd[n][m] == null){
					continue;
				}
				if(bd[n][m] instanceof King){
					board[n][m] = new King(bd[n][m].white);
				}else if(bd[n][m] instanceof Queen){
					board[n][m] = new Queen(bd[n][m].white);
				}else if(bd[n][m] instanceof Bishop){
					board[n][m] = new Bishop(bd[n][m].white);
				}else if(bd[n][m] instanceof Rook){
					board[n][m] = new Rook(bd[n][m].white);
				}else if(bd[n][m] instanceof Knight){
					board[n][m] = new Knight(bd[n][m].white);
				}else if(bd[n][m] instanceof Pawn){
					board[n][m] = new Pawn(bd[n][m].white);
				}
			}
		}
	}
	
	public void analyze(){
		for(Piece[] pa : board){
			for(Piece p : pa){
				if(p != null){
					p.attackers = 0;
					p.defenders = 0;
				}
			}
		}
		for(Piece[] pa : board){
			for(Piece p : pa){
				if(p == null){
					continue;
				}
				try{
					for(Action a : getAllMoves(p)){
						if(board[a.to.x][a.to.y] != null){
							if(board[a.to.x][a.to.y].white == p.white){
								board[a.to.x][a.to.y].defenders++;
							}else{
								board[a.to.x][a.to.y].attackers++;
							}
						}
					}
				}catch(CantMoveException e){}
			}
		}
	}
	
	public void movePlayer(String from, String to) throws FeilInputException{
		int x = 0;
		int x2 = 0;
		for(int n = 0; n < bokstav.length; n++){
			if(bokstav[n].equals(from.substring(0,1))){
				x = n;
			}
			if(bokstav[n].equals(to.substring(0,1))){
				x2 = n;
			}
		}
		int y = Integer.parseInt(from.substring(1,2))-1;
		int y2 = Integer.parseInt(to.substring(1,2))-1;
		
		if(board[x][y] == null){
			throw new FeilInputException();
		}
		board[x2][y2] = board[x][y];
		board[x][y] = null;
	}
	
	public void moveWhite() throws FinnerIkkeBrikkeException{
		ArrayList<Action> options = new ArrayList<Action>();
		//Save the king!
		Piece king = getPieceByNameAndColor("King", true);
		if(king == null){
			throw new FinnerIkkeBrikkeException("Finner ikke kongen");
		}
		
		if(king.attackers == 1){
			System.out.println("Check by one");
			for(Action a : slaBrikke(finnBrikkeSomTruer(king))){
				options.add(a);
			}
			for(Action a : setteIMellom(king)){
				options.add(a);
			}
		}
		if(king.attackers >= 1){
			try{
				Point from = king.findMe(board);
				for(Action a : getAllPlayableMoves(king)){
					options.add(a);
				}
			}catch(CantMoveException e){
			}
			if(!options.isEmpty()){
				options = checkMoves(options, king.white);
				Action a = options.get((int)(options.size()*Math.random()));
				move(a.from, a.to);
				return;
			}
		}
		
		
		//Random movement
		try{
			
			Piece p = findRandomWhitePiece();
			Point to = getRandomMove(p);
			Point from = p.findMe(board);
			move(from, to);
		}catch(CantMoveException e){
			moveWhite();
			return;
		}
		
	/*	for(Piece[] pa : board){
			for(Piece p : pa){
				if(p == null || !p.white){
					continue;
				}
				for(int x = 0; x < board.length; x++){
					for(int y = 0; y < board[x].length; y++){
						
						if(board[x][y] != p){
							if(p.canTake(board, new Point(x, y))){
								
								if(board[x][y] != null){
									if(board[x][y].white){
										continue;
									}else if(board[x][y].defenders >= board[x][y].attackers){
										continue;
									}else{
										board[x][y] = p;
										Point pos = p.findMe(board);
										board[pos.x][pos.y] = null;
										System.out.println(p.symbol + "x" + bokstav[x] + (y+1));
										return;
									}
								}else{
									board[x][y] = p;
									Point pos = p.findMe(board);
									board[pos.x][pos.y] = null;
									System.out.println(p.symbol + bokstav[x] + (y+1));
									return;
								}
							}
						}
					}
				}
			}
		}*/
	}
	
	
	private ArrayList<Action> setteIMellom(Piece king){
		ArrayList<Action> options = new ArrayList<Action>();
		for(Piece[] pa : board){
			for(Piece p : pa){
				if(p == null || p == king || p.white == king.white){
					continue;
				}
				Point from = p.findMe(board);
				Point to = king.findMe(board);
				String movementType = p.getMovementType(from, to, board, true);
				if(movementType.equals("Horse") || movementType.equals("Pawn") || movementType.equals("King")){ //Kan ikke sette noe i veien
					break;
				}else if(movementType.equals("StrightDown")){//Flytt en brukke i veien
					for(int na = from.y; na > to.y; na--){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(from.x, na))){
								options.add(new Action(temp.findMe(board), new Point(from.x, na)));
							}
						}
					}
				}else if(movementType.equals("StrightUp")){//Flytt en brukke i veien
					
					for(int na = from.y; na < to.y; na++){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(from.x, na))){
								options.add(new Action(temp.findMe(board), new Point(from.x, na)));
							}
						}
					}
				}else if(movementType.equals("StrightLeft")){//Flytt en brukke i veien
					
					for(int na = from.x; na > to.x; na--){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(na, from.y))){
								options.add(new Action(temp.findMe(board), new Point(na, from.y)));
							}
						}
					}
				}else if(movementType.equals("StrightRight")){//Flytt en brukke i veien
					
					for(int na = from.x; na < to.x; na++){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(na, from.y))){
								options.add(new Action(temp.findMe(board), new Point(na, from.y)));
							}
						}
					}
				}else if(movementType.equals("RightDown")){//Flytt en brukke i veien
					
					for(int na = 1; na < to.x; na++){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(from.x+na, from.y-na))){
								options.add(new Action(temp.findMe(board), new Point(from.x+na, from.y-na)));
							}
						}
					}
				}else if(movementType.equals("RightUp")){//Flytt en brukke i veien
					
					for(int na = 1; na < to.x; na++){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(from.x+na, from.y+na))){
								options.add(new Action(temp.findMe(board), new Point(from.x+na, from.y+na)));
							}
						}
					}
				}else if(movementType.equals("LeftDown")){//Flytt en brukke i veien
					
					for(int na = 1; na < to.x; na++){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(from.x-na, from.y-na))){
								options.add(new Action(temp.findMe(board), new Point(from.x-na, from.y-na)));
							}
						}
					}
				}else if(movementType.equals("LeftUp")){//Flytt en brukke i veien
					
					for(int na = 1; na < to.x; na++){
						for(Piece temp : pa){
							if(temp == null || temp.white != king.white){
								continue;
							}
							if(temp.canTake(board, new Point(from.x-na, from.y+na))){
								options.add(new Action(temp.findMe(board), new Point(from.x-na, from.y+na)));
							}
						}
					}
				}
			}
		}
		return options;
	}
	
	Piece finnBrikkeSomTruer(Piece p){
		for(Piece[] pa : board){
			for(Piece pt : pa){
				if(pt == null || pt == p || p.white == pt.white){
					continue;
				}else if(pt.canTake(board, p.findMe(board))){
					return pt;
				}
			}
		}
		return null;
	}
	
	ArrayList<Action> slaBrikke(Piece p){
		ArrayList<Action> options = new ArrayList<Action>();
		for(Piece[] pa : board){
			for(Piece pt : pa){
				if(pt == null || pt == p || p.white == pt.white){
					continue;
				}else if(pt.canTake(board, p.findMe(board))){
	//				System.out.println("Kan trekke " + bokstav[from.x] + (from.y+1) + " - " + bokstav[to.x] + (to.y+1));
					options.add(new Action(pt.findMe(board), p.findMe(board)));
				}
			}
		}
		return options;
	}
	
	private ArrayList<Action> checkMoves(ArrayList<Action> op, boolean white){
		ArrayList<Action> options = new ArrayList<Action>();
		for(Action a : op){
			a.print();
			Chessboard c = new Chessboard(board);
			c.move(a);
			c.analyze();
			System.out.println(c.getPieceByNameAndColor("King", white).attackers);
			if(c.getPieceByNameAndColor("King", white).attackers > 0){
				continue;
			}
			options.add(a);
		}
		return options;
	}
	
	void move(Point from, Point to){
		Piece p = board[from.x][from.y];
		if(board[to.x][to.y] != null){
			System.out.println(p.symbol + "x" + bokstav[to.x] + (to.y+1));
		}else{
			System.out.println(p.symbol + bokstav[to.x] + (to.y+1));
		}
		
		board[from.x][from.y] = null;
		board[to.x][to.y] = p;
	}
	void move(Action a){
		move(a.from, a.to);
	}
	
	public Piece getPieceByNameAndColor(String s, boolean white){
		for(Piece[] pa : board){
			for(Piece p : pa){
				if(p == null){
					continue;
				}else if(p.white == white && p.name.equals(s)){
					return p;
				}
			}
		}
		return null;
	}
	
	private Piece findRandomWhitePiece(){
		ArrayList<Piece> whitePices = new ArrayList<Piece>();
		
		for(Piece[] pa : board){
			for(Piece p : pa){
				if(p == null){
					continue;
				}else if(p.white){
					whitePices.add(p);
				}
			}
		}
		
		return whitePices.get((int)(whitePices.size()*Math.random()));
	}
	
	
	private Point getRandomMove(Piece p) throws CantMoveException{
		Action[] moves = getAllPlayableMoves(p);
		
		return moves[(int)(moves.length*Math.random())].to;
	}
	private Action[] getAllPlayableMoves(Piece p) throws CantMoveException{
		ArrayList<Action> pm = new ArrayList<Action>();
		Action[] moves = getAllMoves(p);
		
		for(Action a : moves){
			if(board[a.to.x][a.to.y] != null){
				if(board[a.to.x][a.to.y].white != p.white){
					pm.add(a);
				}
			}else{
				pm.add(a);
			}
		}
		if(pm.isEmpty()){
			throw new CantMoveException();
		}
		return pm.toArray(new Action[0]);
	}
	
	private Action[] getAllMoves(Piece p) throws CantMoveException{
		ArrayList<Action> moves = new ArrayList<Action>();
		
		Point me = p.findMe(board);
		
		for(int n = 0; n < board.length; n++){
			for(int m = 0; m < board[n].length; m++){
				if(p.canTake(board, new Point(n, m)) && !(me.x == n && me.y == m)){
					moves.add(new Action(p.findMe(board), new Point(n, m)));
				}
			}
		}
		if(moves.isEmpty()){
			throw new CantMoveException();
		}
		return moves.toArray(new Action[0]);
	}
	
	public void print(){
		for(Piece[] pi : board){
			for(Piece p : pi){
				if(p != null){
					System.out.print(p.name + "\t");
				}else{
					System.out.print("-\t");
				}
			}
			System.out.println();
		}
	}
	public void printDefenders(){
		for(Piece[] pi : board){
			for(Piece p : pi){
				if(p != null){
					System.out.print(p.defenders + " ");
				}else{
					System.out.print("- ");
				}
			}
			System.out.println();
		}
	}
	public void printAttackers(){
		for(Piece[] pi : board){
			for(Piece p : pi){
				if(p != null){
					System.out.print(p.attackers + " ");
				}else{
					System.out.print("- ");
				}
			}
			System.out.println();
		}
	}
	
}

class Piece{
	String name;
	String symbol;
	int value;
	boolean white;
	int defenders;
	int attackers;
	
	Piece(){
		name = "";
	}
	
	public boolean canTake(Piece[][] board, Point from){
		return false;
	}
	
	public boolean inDanger(){
		return defenders >= attackers;
	}
	
	public int getValue(){
		return value;
	}
	
	public String getName(){
		return name;
	}
	
	public Point findMe(Piece[][] board){
		for(int n =0; n < board.length; n++){
			for(int m = 0; m < board[n].length; m++){
				if(this == board[n][m]){
					return new Point(n, m);
				}
			}
		}
		return null;
	}
	
	public String getMovementType(Point from, Point to, Piece[][] board, boolean white){
		if(checkPathHorse(from, to, board)){
			return "Horse";
		}else if(checkPathKing(from, to, board)){
			return "King";
		}else if(checkPathPawn(from, to, board, !white)){
			return "Pawn";
		}else if(checkPathStright(from, to, board)){
			if(checkPathStrightDown(from, to, board)){
				return "StrightDown";
			}else if(checkPathStrightLeft(from, to, board)){
				return "StrightLeft";
			}else if(checkPathStrightUp(from, to, board)){
				return "StrightUp";
			}else if(checkPathStrightRight(from, to, board)){
				return "StrightRight";
			}
		}else if(checkPathDiagonal(from, to, board)){
			if(checkPathRightDown(from, to, board)){
				return "RightDown";
			}else if(checkPathRightUp(from, to, board)){
				return "RightUp";
			}else if(checkPathLeftDown(from, to, board)){
				return "LeftDown";
			}else if(checkPathLeftUp(from, to, board)){
				return "LeftUp";
			}
		}
		return "";
	}
	
	boolean checkPathStright(Point from, Point to, Piece[][] board){
		if(from.x == to.x){
			if(from.y < to.y){
				return checkPathStrightUp(from, to, board);
			}else{
				return checkPathStrightDown(from, to, board);
			}
		}else if(from.y == to.y){
			if(from.x < to.x){
				return checkPathStrightRight(from, to, board);
			}else{
				return checkPathStrightLeft(from, to, board);
			}
		}
		return false;
	}
	private boolean checkPathStrightUp(Point from, Point to, Piece[][] board){
		for(int n = from.y+1; n < to.y; n++){
			if(board[from.x][n] != null){
				return false;
			}
		}
		return true;
	}
	private boolean checkPathStrightDown(Point from, Point to, Piece[][] board){
		for(int n = from.y-1; n > to.y; n--){
			if(board[from.x][n] != null){
				return false;
			}
		}
		return true;
	}
	private boolean checkPathStrightRight(Point from, Point to, Piece[][] board){
		for(int n = from.x+1; n < to.x; n++){
			if(board[n][from.y] != null){
				return false;
			}
		}
		return true;
	}
	private boolean checkPathStrightLeft(Point from, Point to, Piece[][] board){
		for(int n = from.x-1; n > to.x; n--){
			if(board[n][from.y] != null){
				return false;
			}
		}
		return true;
	}
	
	boolean checkPathDiagonal(Point from, Point to, Piece[][] board){
		if(Math.abs(from.x - to.x) == Math.abs(from.y - to.y)){
			if(from.x < to.x && from.y < to.y){
				return checkPathRightUp(from, to, board);
			}else if(from.x < to.x && from.y > to.y){
				return checkPathRightDown(from, to, board);
			}else if(from.x > to.x && from.y < to.y){
				return checkPathLeftUp(from, to, board);
			}else if(from.x > to.x && from.y > to.y){
				return checkPathLeftDown(from, to, board);
			}
		}
		return false;
	}
	private boolean checkPathRightUp(Point from, Point to, Piece[][] board){
		for(int n = from.x+1; n < to.x; n++){
			for(int m = from.y+1; m < to.y; m++){
				if(board[n][m] != null){
					return false;
				}
			}
		}
		return true;
	}
	private boolean checkPathRightDown(Point from, Point to, Piece[][] board){
		for(int n = from.x+1; n < to.x; n++){
			for(int m = from.y-1; m > to.y; m--){
				if(board[n][m] != null){
					return false;
				}
			}
		}
		return true;
	}
	private boolean checkPathLeftUp(Point from, Point to, Piece[][] board){
		for(int n = from.x-1; n > to.x; n--){
			for(int m = from.y+1; m < to.y; m++){
				if(board[n][m] != null){
					return false;
				}
			}
		}
		return true;
	}
	private boolean checkPathLeftDown(Point from, Point to, Piece[][] board){
		for(int n = from.x-1; n > to.x; n--){
			for(int m = from.y-1; m > to.y; m--){
				if(board[n][m] != null){
					return false;
				}
			}
		}
		return true;
	}
	
	boolean checkPathHorse(Point from, Point to, Piece[][] board){
		int[] temp1 = {-2, -1, 1, 2,  2,  1, -1, -2};
		int[] temp2 = { 1,  2, 2, 1, -1, -2, -2, -1};
		for(int n = 0; n < temp1.length; n++){
			if(from.x+temp1[n] == to.x && from.y+temp2[n] == to.y){
				return true;
			}
		}
		return false;
	}
	boolean checkPathPawn(Point from, Point to, Piece[][] board, boolean white){
		if(white){
			if((from.x+1 == to.x || from.x-1 == to.x) && from.y+1 == to.y && board[to.x][to.y] != null){
				return true;
			}else if(from.x == to.x && (from.y+1 == to.y || (from.y+2 == to.y && from.y == 1))){
				return true;
			}
		}else{
			if((from.x+1 == to.x || from.x-1 == to.x) && from.y-1 == to.y && board[to.x][to.y] != null){
				return true;
			}else if(from.x == to.x && (from.y-1 == to.y || (from.y-2 == to.y && from.y == 6))){
				return true;
			}
		}
		return false;
	}
	boolean checkPathKing(Point from, Point to, Piece[][] board){
		if(Math.abs(from.x-to.x) <= 1 && Math.abs(from.y-to.y) <= 1){
			return true;
		}
		return false;
	}
	
}

class Queen extends Piece{
	
	Queen(boolean white){
		name = "Queen";
		symbol = "Q";
		this.white = white;
		value = 9;
	}
	
	public boolean canTake(Piece[][] board, Point take){
		Point pos = findMe(board);
		if(checkPathStright(pos, take, board)){
			return true;
		}else if(checkPathDiagonal(pos, take, board)){
			return true;
		}
		return false;
	}
}
class Rook extends Piece{
	
	Rook(boolean white){
		name = "Rook";
		symbol = "R";
		this.white = white;
		value = 5;
	}
	
	public boolean canTake(Piece[][] board, Point take){
		Point pos = findMe(board);
		if(checkPathStright(pos, take, board)){
			return true;
		}
		return false;
	}
}
class Bishop extends Piece{
	
	Bishop(boolean white){
		name = "Bishop";
		symbol = "B";
		this.white = white;
		value = 3;
	}
	
	public boolean canTake(Piece[][] board, Point take){
		Point pos = findMe(board);
		if(checkPathDiagonal(pos, take, board)){
			return true;
		}
		return false;
	}
}
class Knight extends Piece{
	
	Knight(boolean white){
		name = "Knight";
		symbol = "N";
		this.white = white;
		value = 3;
	}
	
	public boolean canTake(Piece[][] board, Point take){
		Point pos = findMe(board);
		if(checkPathHorse(pos, take, board)){
			return true;
		}
		return false;
	}
}
class Pawn extends Piece{
	
	boolean hasMoved = false;
	
	Pawn(boolean white){
		name = "Pawn";
		symbol = "";
		this.white = white;
		value = 1;
	}
	
	public boolean canTake(Piece[][] board, Point take){
		Point pos = findMe(board);
		if(checkPathPawn(pos, take, board, white)){
			return true;
		}
		return false;
	}
}
class King extends Piece{
	
	King(boolean white){
		name = "King";
		symbol = "K";
		this.white = white;
		value = 99999999;
	}
	
	public boolean canTake(Piece[][] board, Point take){
		Point pos = findMe(board);
		if(checkPathKing(pos, take, board)){
			return true;
		}
		return false;
	}
}

class Point{
	int x, y;
	
	Point(int x, int y){
		this.x = x;
		this.y = y;
	}
}

class CantMoveException extends Exception{}
class FeilInputException extends Exception{}
class FinnerIkkeBrikkeException extends Exception{
	String melding = "";
	FinnerIkkeBrikkeException(String e){
		melding = e;
	}
}

class Action{
	String[] bokstav = {"a", "b", "c", "d", "e", "f", "g", "h"};
	Point from, to;
	
	Action(Point from, Point to){
		this.from = from;
		this.to = to;
	}
	
	public void print(){
		System.out.println(bokstav[from.x] + (from.y+1) + " - " + bokstav[to.x] + (to.y+1));
	}
}

